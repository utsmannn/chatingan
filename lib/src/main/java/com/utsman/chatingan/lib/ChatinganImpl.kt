package com.utsman.chatingan.lib

import android.content.Context
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.utsman.chatingan.common.IOScope
import com.utsman.chatingan.lib.data.ChatinganException
import com.utsman.chatingan.lib.data.entity.ContactEntity
import com.utsman.chatingan.lib.data.firebase.FirebaseMessageRequest
import com.utsman.chatingan.lib.data.model.Contact
import com.utsman.chatingan.lib.data.model.Message
import com.utsman.chatingan.lib.data.model.MessageInfo
import com.utsman.chatingan.lib.data.model.MessageReport
import com.utsman.chatingan.lib.database.ChatinganDao
import com.utsman.chatingan.lib.database.ChatinganDatabase
import com.utsman.chatingan.lib.paging.MessagePagingSources
import com.utsman.chatingan.lib.receiver.MessageNotifier
import com.utsman.chatingan.lib.services.FirebaseWebServices
import com.utsman.chatingan.lib.utils.ChatinganDividerUtils
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*

class ChatinganImpl(
    private val context: Context
) : Chatingan {

    private val database: ChatinganDatabase
        get() {
            return ChatinganDatabase.getInstance(context)
        }

    private val chatinganDao: ChatinganDao
        get() = database.chatinganDao()

    private val webServices: FirebaseWebServices
        get() = FirebaseWebServices.getInstance(config)

    private val config: ChatinganConfiguration
        get() = getConfiguration()

    override fun getConfiguration(): ChatinganConfiguration {
        return ChatinganConfiguration.getPref(context)
    }

    private val newMessageFlow: MutableStateFlow<Pair<Contact, Message>?> = MutableStateFlow(null)
    private val sdfDay = SimpleDateFormat("DD")
    private var nowDay = sdfDay.format(Date.from(Instant.now())).toInt()

    private val _chatinganQr by lazy {
        ChatinganQrImpl(getConfiguration().contact) { contactPaired, qrImpl ->
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
                notificationType = MessageNotifier.NotificationType.CONTACT_PAIR,
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

    override fun bindToFirebaseMessagingServices(
        data: Map<String, String>,
        onMessageIncoming: (Contact, Message) -> Unit
    ) {
        val incomingNotification = MessageNotifier.fromMap(data)
        when (incomingNotification.type) {
            MessageNotifier.NotificationType.MESSAGE -> {
                IOScope().launch {
                    saveMessageFromNotification(incomingNotification, onMessageIncoming)
                }
            }
            MessageNotifier.NotificationType.CONTACT_PAIR -> {
                IOScope().launch {
                    saveContactPairFromNotification(incomingNotification)
                }
            }
            MessageNotifier.NotificationType.TYPING -> {
                IOScope().launch {
                    updateContactTyping(incomingNotification)
                }
            }
            MessageNotifier.NotificationType.MESSAGE_REPORT -> {
                IOScope().launch {
                    updateMessageStatus(incomingNotification)
                }
            }
            MessageNotifier.NotificationType.CONTACT_UPDATE -> {
                IOScope().launch {
                    updateContact(incomingNotification)
                }
            }
            else -> {

            }
        }
    }

    override fun updateFcmToken(fcmToken: String) {
        val currentContact = config.contact
        val updatedContact = currentContact.copy(fcmToken = fcmToken)
        val updatedContactEntity = DataMapper.mapContactToEntity(updatedContact)
        IOScope().launch {
            chatinganDao.getAllContact()
                .firstOrNull()
                ?.onEach {
                    val targetToken = it.fcmToken
                    sendNotification(
                        token = targetToken,
                        json = updatedContactEntity.toJson(),
                        notificationType = MessageNotifier.NotificationType.CONTACT_UPDATE,
                        messageType = Message.Type.OTHER
                    )
                }
        }
    }

    override suspend fun onMessageUpdate(contact: Contact, newMessage: suspend (Message?) -> Unit) {
        newMessageFlow.filterNotNull().collect { (con, msg) ->
            if (contact.id == con.id) {
                newMessage.invoke(msg)
            } else {
                newMessageFlow.value = null
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
            sendNotification(
                token = entity.fcmToken,
                json = config.contact.toJson(),
                notificationType = MessageNotifier.NotificationType.CONTACT_PAIR,
                messageType = Message.Type.OTHER,
                title = config.contact.name,
                subtitle = "Success add ${config.contact.name}"
            )

            _chatinganQr.setPairListenerSuccess(contact)
        }
    }

    override fun getAllContact(): Flow<List<Contact>> {
        return chatinganDao.getAllContact().map { contacts ->
            contacts.map { entity -> DataMapper.mapEntityToContact(entity) }
        }
    }

    override fun getContactByEmail(email: String): Flow<Contact> {
        return flow {
            val entity = chatinganDao.getContactByEmail(email)
                ?: throw ChatinganException("Contact not found!")
            val contact = DataMapper.mapEntityToContact(entity)
            emit(contact)
        }
    }

    override fun getContact(contactId: String): Flow<Contact> {
        return chatinganDao.getContactFlow(contactId).map { DataMapper.mapEntityToContact(it) }
    }

    override suspend fun getMessagesInfo(): Flow<List<MessageInfo>> {
        return chatinganDao.getContactAndLastMessages().map { list ->
            list
                .map {
                    val senderId = it.contactEntity.id
                    val unreadCount =
                        chatinganDao.getUnreadCount(senderId, Message.Status.READ.name)
                    DataMapper.mapContactAndLastMessageToMessageInfo(it, unreadCount)
                }
                .filter {
                    it.lastMessage.isNotEmpty()
                }
        }
    }

    override suspend fun sendMessage(contact: Contact, message: Message) {
        newMessageFlow.value = Pair(contact, message)
        when (message) {
            is Message.TextMessages -> {
                sendTextMessage(contact, message)
            }
            else -> {

            }
        }
    }

    override fun getAllMessages(
        contact: Contact,
        withDivider: Boolean,
        asReversed: Boolean
    ): Flow<List<Message>> {
        return chatinganDao.getAllMessage(contact.id)
            .map { messages ->
                messages.map { entity ->
                    DataMapper.mapEntityToMessage(entity)
                }.run {
                    if (withDivider) {
                        ChatinganDividerUtils.calculateDividerChat(this)
                    } else {
                        this
                    }
                }
            }
            .distinctUntilChanged()
    }

    override fun getAllMessage(
        contact: Contact,
        withDivider: Boolean,
        size: Int,
        isOnlySnapshot: Boolean
    ): Flow<PagingData<Message>> {
        newMessageFlow.value = null
        val pager = Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false,
            ),
            pagingSourceFactory = {
                if (isOnlySnapshot) {
                    MessagePagingSources(contact, chatinganDao)
                } else {
                    chatinganDao.getAllMessagePagedSources(contact.id)
                }
            }
        )

        return pager
            .flow
            .distinctUntilChanged()
            .map {
                it.map { entity ->
                    /*if (withDivider) {
                        val currentDay = sdfDay.format(entity.date.toDate()).toInt()
                        if (currentDay < nowDay) {
                            DataMapper.mapEntityToMessageDivider(entity)
                        } else {
                            DataMapper.mapEntityToMessage(entity)
                        }.also {
                            nowDay = currentDay
                        }
                    } else {
                        DataMapper.mapEntityToMessage(entity)
                    }*/
                    DataMapper.mapEntityToMessage(entity)
                }
            }

    }

    override fun getLastMessage(messageInfo: MessageInfo): Flow<Message> {
        return chatinganDao.getLastMessage(messageInfo.receiver.id).map {
            DataMapper.mapEntityToMessage(it)
        }
    }

    override suspend fun markMessageIsRead(contact: Contact, message: Message) {
        val messageReport = MessageReport(message.getChildId(), Message.Status.READ)
        sendNotification(
            token = contact.fcmToken,
            json = messageReport.toJson(),
            notificationType = MessageNotifier.NotificationType.MESSAGE_REPORT,
            messageType = Message.Type.OTHER
        )
        chatinganDao.updateMessageStatus(messageReport.messageId, messageReport.status.name)
    }

    override suspend fun setTyping(contact: Contact, isTyping: Boolean) {
        val contactEntity = DataMapper.mapContactToEntity(config.contact)
            .setTyping(isTyping)

        val content = contactEntity.toJson()
        sendNotification(
            token = contact.fcmToken,
            json = content,
            notificationType = MessageNotifier.NotificationType.TYPING,
            messageType = Message.Type.OTHER
        )
    }

    private suspend fun sendTextMessage(contact: Contact, textMessages: Message.TextMessages) {
        val messageJson = textMessages.toJson()
        val messageEntity = DataMapper.mapMessageToEntity(textMessages)

        chatinganDao.insertMessage(messageEntity)

        val receiver = contact.copy(lastMessageId = messageEntity.id)
        val isNotificationSuccess = sendNotification(
            token = receiver.fcmToken,
            json = messageJson,
            notificationType = MessageNotifier.NotificationType.MESSAGE,
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
        val updatedContactEntity = DataMapper.mapContactToEntity(receiver)
        chatinganDao.updateContact(updatedContactEntity)

        val updatedContactMe = config.contact.copy(lastMessageId = newMessageEntity.id)
        config.updateContact(updatedContactMe)

        chatinganDao.updateMessage(newMessageEntity)
    }

    private suspend fun saveMessageFromNotification(
        notification: MessageNotifier,
        onMessageIncoming: (Contact, Message) -> Unit
    ) {
        if (notification.type != MessageNotifier.NotificationType.MESSAGE) return
        when (notification.messageType) {
            Message.Type.TEXT -> {
                val textMessage: Message.TextMessages = Utils.convertFromJson(notification.body)
                val newTextMessage = textMessage.updateStatus(Message.Status.RECEIVED)
                val textMessageEntity = DataMapper.mapMessageToEntity(newTextMessage)
                chatinganDao.insertMessage(textMessageEntity)

                val contactEntity = chatinganDao.getContactById(textMessage.senderId)
                    ?: throw ChatinganException("Contact not found!")
                val contact = DataMapper
                    .mapEntityToContact(contactEntity)
                    .copy(lastMessageId = textMessage.id)
                val newContactEntity = DataMapper.mapContactToEntity(contact)

                val messageReport = MessageReport(textMessage.id, Message.Status.RECEIVED)
                sendNotification(
                    token = contact.fcmToken,
                    json = messageReport.toJson(),
                    notificationType = MessageNotifier.NotificationType.MESSAGE_REPORT,
                    messageType = Message.Type.OTHER
                )
                chatinganDao.updateContact(newContactEntity)
                sendToServices(textMessage.id, onMessageIncoming)

                newMessageFlow.value = Pair(contact, textMessage)
            }
            Message.Type.IMAGE -> {

            }
            else -> {

            }
        }
    }

    private suspend fun sendToServices(
        messageId: String,
        onMessageIncoming: (Contact, Message) -> Unit
    ) {
        delay(1000)
        if (!chatinganDao.isMessageMatchStatus(messageId, Message.Status.READ.name)) {
            val messageAndSender = chatinganDao.getMessageAndSender(messageId)
                .first()

            val senderEntity = messageAndSender.contactEntity.firstOrNull()
            val messageEntity = messageAndSender.messages
            if (senderEntity != null) {
                val sender = DataMapper.mapEntityToContact(senderEntity)
                val message = DataMapper.mapEntityToMessage(messageEntity)
                onMessageIncoming.invoke(sender, message)
            }
        }
    }

    private suspend fun saveContactPairFromNotification(notification: MessageNotifier) {
        if (notification.type != MessageNotifier.NotificationType.CONTACT_PAIR) return
        val contact: Contact = Utils.convertFromJson(notification.body)
        val contactEntity = DataMapper.mapContactToEntity(contact)
        if (!chatinganDao.isContactExist(contactEntity.email)) {
            chatinganDao.insertContact(contactEntity)
            MainScope().launch {
                _chatinganQr.setPairListenerSuccess(contact)
            }
        } else {
            MainScope().launch {
                _chatinganQr.setPairListenerFailure(ChatinganException("Contact exist!"))
            }
        }
    }

    private suspend fun updateContactTyping(notification: MessageNotifier) {
        if (notification.type != MessageNotifier.NotificationType.TYPING) return
        val contactEntity: ContactEntity = Utils.convertFromJson(notification.body)
        chatinganDao.updateTypingByEmail(contactEntity.email, contactEntity.isTyping)
    }

    private suspend fun updateMessageStatus(notification: MessageNotifier) {
        if (notification.type != MessageNotifier.NotificationType.MESSAGE_REPORT) return
        val messageReport: MessageReport = Utils.convertFromJson(notification.body)
        chatinganDao.updateMessageStatus(messageReport.messageId, messageReport.status.name)

        val messageEntity = chatinganDao.getMessageById(messageReport.messageId)
        val senderId = messageEntity.senderId
        val contactEntity = if (senderId != config.contact.id) {
            chatinganDao.getContactById(senderId)
        } else {
            chatinganDao.getContactById(messageEntity.receiverId)
        }

        if (contactEntity != null) {
            val contact = DataMapper.mapEntityToContact(contactEntity)
            val message = DataMapper.mapEntityToMessage(messageEntity)

            newMessageFlow.value = Pair(contact, message)
        }
    }

    private suspend fun updateContact(notification: MessageNotifier) {
        if (notification.type != MessageNotifier.NotificationType.CONTACT_UPDATE) return
        val contactEntity: ContactEntity = Utils.convertFromJson(notification.body)
        chatinganDao.updateContact(contactEntity)
    }

    private suspend fun sendNotification(
        token: String,
        json: String,
        notificationType: MessageNotifier.NotificationType,
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

        return response.code() == 200
    }

    companion object {
        private const val ELLIPSIZE_MAX = 20
    }

}