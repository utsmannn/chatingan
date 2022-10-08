package com.utsman.chatingan.lib

import android.content.Context
import com.utsman.chatingan.lib.data.ChatinganException
import com.utsman.chatingan.lib.data.firebase.FirebaseMessageRequest
import com.utsman.chatingan.lib.data.firebase.FirebaseNotification
import com.utsman.chatingan.lib.data.model.Contact
import com.utsman.chatingan.lib.data.model.Message
import com.utsman.chatingan.lib.data.model.MessageInfo
import com.utsman.chatingan.lib.data.pair.ContactPair
import com.utsman.chatingan.lib.data.pair.ContactPairListener
import com.utsman.chatingan.lib.database.ChatinganDao
import com.utsman.chatingan.lib.database.ChatinganDatabase
import com.utsman.chatingan.lib.services.FirebaseWebServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class ChatinganImpl(
    private val context: Context,
    private val config: ChatinganConfiguration
) : Chatingan {

    private val database: ChatinganDatabase
        get() {
            return ChatinganDatabase.getInstance(context)
        }

    private val chatinganDao: ChatinganDao
        get() = database.chatinganDao()

    private val webServices: FirebaseWebServices
        get() = FirebaseWebServices.getInstance(config)

    override fun getConfiguration(): ChatinganConfiguration {
        return config
    }

    private val _chatinganQr by lazy {
        ChatinganQrImpl(config) { contactPaired, qrImpl ->
            val contentContact = config.contact.toJson()
            val existingContact = chatinganDao.getContactByEmail(contactPaired.email)
            if (existingContact != null) {
                val throwable = ChatinganException("Contact exist!")
                qrImpl.setPairListenerFailure(throwable)
                throw throwable
            }

            val isSuccessRequest = sendNotification(
                token = contactPaired.fcmToken,
                json = contentContact,
                notificationType = FirebaseNotification.NotificationType.CONTACT_PAIR,
                messageType = Message.Type.OTHER
            )

            val contactEntity = DataMapper.mapContactToEntity(contactPaired)
            chatinganDao.insertContact(contactEntity)
            qrImpl.setPairListenerSuccess(contactPaired)

            return@ChatinganQrImpl isSuccessRequest
        }
    }

    override fun getChatinganQr(): ChatinganQr {
        return _chatinganQr
    }

    override fun bindToFirebaseMessagingServices(data: Map<String, String>) {
        val incomingNotification = FirebaseNotification.fromMap(data)
        CoroutineScope(Dispatchers.IO).launch {
            when (incomingNotification.type) {
                FirebaseNotification.NotificationType.MESSAGE -> {
                    saveMessageFromNotification(incomingNotification)
                }
                FirebaseNotification.NotificationType.CONTACT_PAIR -> {
                    saveContactPairFromNotification(incomingNotification)
                }
                else -> {

                }
            }
        }
    }

    override suspend fun addContact(contact: Contact) {
        val entity = DataMapper.mapContactToEntity(contact)
        if (chatinganDao.isContactExist(entity.email)) throw ChatinganException("Contact exists")

        chatinganDao.insertContact(entity)
    }

    override suspend fun pairContact(contact: Contact) {
        val entity = DataMapper.mapContactToEntity(contact)
        if (chatinganDao.isContactExist(entity.email)) {
            val throwable = ChatinganException("Contact exists")
            _chatinganQr.setPairListenerFailure(throwable)
        } else {
            chatinganDao.insertContact(entity)
            val contactPair = DataMapper.mapContactToPairData(config.contact)
            sendNotification(
                token = entity.fcmToken,
                json = contactPair.toJson(),
                notificationType = FirebaseNotification.NotificationType.CONTACT_PAIR,
                messageType = Message.Type.OTHER,
                title = contactPair.name,
                subtitle = "Success add ${contactPair.name}"
            )

            _chatinganQr.setPairListenerSuccess(contact)
        }
    }

    override suspend fun getAllContact(): Flow<List<Contact>> {
        return chatinganDao.getAllContact().map { contacts ->
            contacts.map { entity -> DataMapper.mapEntityToContact(entity) }
        }
    }

    override suspend fun getContactByEmail(email: String): Flow<Contact> {
        return flow {
            val entity = chatinganDao.getContactByEmail(email)
                ?: throw ChatinganException("Contact not found!")
            val contact = DataMapper.mapEntityToContact(entity)
            emit(contact)
        }
    }

    override suspend fun getMessagesInfo(): Flow<List<MessageInfo>> {
        return chatinganDao.getAllMessageInfoAndReceiverContact().map { transaction ->
            transaction.map { messagesInfoAndReceiverContact ->
                if (messagesInfoAndReceiverContact.messageInfoEntities.isNotEmpty()) {
                    DataMapper.mapMessagesInfoAndReceiverContactToMessagesInfo(
                        messagesInfoAndReceiverContact
                    )
                } else {
                    val contact = DataMapper.mapEntityToContact(
                        messagesInfoAndReceiverContact.receiverEntity
                    )
                    MessageInfo.empty(contact)
                }
            }.filter {
                it.id.isNotEmpty()
            }
        }
    }

    override suspend fun sendMessage(message: Message) {
        when (message) {
            is Message.TextMessages -> {
                sendTextMessage(message)
            }
            else -> {

            }
        }
    }

    override suspend fun getMessages(messageInfo: MessageInfo): Flow<List<Message>> {
        return chatinganDao.getAllMessage(messageInfo.id)
            .map { messages ->
                messages.map { entity -> DataMapper.mapEntityToMessage(entity) }
            }
    }

    override suspend fun getLastMessage(messageInfo: MessageInfo): Flow<Message> {
        return chatinganDao.getLastMessage(messageInfo.id).map { DataMapper.mapEntityToMessage(it) }
    }

    private suspend fun sendTextMessage(textMessages: Message.TextMessages) {
        val currentMessageInfoId = textMessages.messageInfoId
        if (!chatinganDao.isMessageInfoExist(currentMessageInfoId)) {
            val receiver = chatinganDao.getContactById(textMessages.receiverId)
                ?: throw ChatinganException("Contact not found")
            val newMessageInfoEntity = MessageInfo(
                id = currentMessageInfoId,
                receiver = receiver.run { DataMapper.mapEntityToContact(this) }
            ).run { DataMapper.mapMessageInfoToEntity(this) }
            chatinganDao.insertMessageInfo(newMessageInfoEntity)
        }

        val currentMessageInfo = chatinganDao.getMessageInfoAndReceiverContact(currentMessageInfoId)
            .run {
                DataMapper.mapMessagesInfoAndReceiverContactToMessagesInfo(this)
            }

        val receiver = currentMessageInfo.receiver
        val messageJson = textMessages.toJson()

        val messageEntity = DataMapper.mapMessageToEntity(textMessages)
        chatinganDao.insertMessage(messageEntity)

        val isNotificationSuccess = sendNotification(
            token = receiver.fcmToken,
            json = messageJson,
            notificationType = FirebaseNotification.NotificationType.MESSAGE,
            messageType = Message.Type.TEXT,
            title = receiver.name,
            subtitle = textMessages.messageBody.ellipsize(ELLIPSIZE_MAX)
        )

        val messageStatus = if (isNotificationSuccess) {
            Message.Status.SENT
        } else {
            Message.Status.FAILURE
        }
        val newMessageEntity = messageEntity.changeStatus(messageStatus)
        chatinganDao.updateMessage(newMessageEntity)
    }

    private suspend fun saveMessageFromNotification(notification: FirebaseNotification) {
        if (notification.type != FirebaseNotification.NotificationType.MESSAGE) return
        when (notification.messageType) {
            Message.Type.TEXT -> {
                val textMessage: Message.TextMessages = Utils.convertFromJson(notification.body)
                val currentMessageInfo = chatinganDao.getMessageInfoById(textMessage.messageInfoId)

                chatinganDao.insertMessage(DataMapper.mapMessageToEntity(textMessage))

                val updatedMessageInfo = currentMessageInfo.updateTime()
                chatinganDao.updateMessageInfo(updatedMessageInfo)
            }
            Message.Type.IMAGE -> {

            }
            else -> {

            }
        }
    }

    private suspend fun saveContactPairFromNotification(notification: FirebaseNotification) {
        if (notification.type != FirebaseNotification.NotificationType.CONTACT_PAIR) return
        val contactPair: ContactPair = Utils.convertFromJson(notification.body)
        val contactPaired = DataMapper.mapContactPairToContact(contactPair)
        val contactEntity = DataMapper.mapContactToEntity(contactPaired)
        if (!chatinganDao.isContactExist(contactEntity.email)) {
            chatinganDao.insertContact(contactEntity)
            MainScope().launch {
                _chatinganQr.setPairListenerSuccess(contactPaired)
            }
        } else {
            MainScope().launch {
                _chatinganQr.setPairListenerFailure(ChatinganException("Contact exist!"))
            }
        }
    }

    private suspend fun sendNotification(
        token: String,
        json: String,
        notificationType: FirebaseNotification.NotificationType,
        messageType: Message.Type,
        title: String = "",
        subtitle: String = ""
    ): Boolean {
        val firebaseRequest = FirebaseMessageRequest.createFromMessage(token = token) {
            body = json
            type = notificationType

            this.messageType = messageType
            if (title.isNotEmpty()) {
                this.title = title
            }
            if (subtitle.isNotEmpty()) {
                this.subtitle = subtitle
            }
        }

        val response = webServices.sendMessage(firebaseRequest)
        if (!response.isSuccessful) return false

        val body = response.body()
        return body?.failure == null
    }

    companion object {
        private const val ELLIPSIZE_MAX = 20
    }

}