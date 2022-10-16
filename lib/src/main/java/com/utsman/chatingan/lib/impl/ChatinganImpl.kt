package com.utsman.chatingan.lib.impl

import android.content.Context
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.utsman.chatingan.lib.Chatingan
import com.utsman.chatingan.lib.ChatinganQr
import com.utsman.chatingan.lib.data.ChatinganException
import com.utsman.chatingan.lib.data.entity.ContactEntity
import com.utsman.chatingan.lib.data.model.Contact
import com.utsman.chatingan.lib.data.model.Message
import com.utsman.chatingan.lib.data.model.MessageInfo
import com.utsman.chatingan.lib.data.model.MessageReport
import com.utsman.chatingan.lib.database.ChatinganDao
import com.utsman.chatingan.lib.database.ChatinganDatabase
import com.utsman.chatingan.lib.paging.MessagePagingSources
import com.utsman.chatingan.lib.preferences.ChatinganPreferences
import com.utsman.chatingan.lib.provider.ImageUploader
import com.utsman.chatingan.lib.provider.MessageEmitter
import com.utsman.chatingan.lib.receiver.MessageNotifier
import com.utsman.chatingan.lib.utils.ChatinganDividerUtils
import com.utsman.chatingan.lib.utils.DataMapper
import com.utsman.chatingan.lib.utils.IOScope
import com.utsman.chatingan.lib.utils.Utils
import com.utsman.chatingan.lib.utils.changeStatus
import com.utsman.chatingan.lib.utils.getContact
import com.utsman.chatingan.lib.utils.now
import com.utsman.chatingan.lib.utils.setTyping
import com.utsman.chatingan.lib.utils.toDate
import com.utsman.chatingan.lib.utils.toJson
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
import java.util.*

class ChatinganImpl(
    private val context: Context,
    private val messageEmitter: MessageEmitter,
    private val imageUploader: ImageUploader
) : Chatingan {

    private val database: ChatinganDatabase
        get() {
            return ChatinganDatabase.getInstance(context)
        }

    private val chatinganDao: ChatinganDao
        get() = database.chatinganDao()

    override fun getContact(): Contact {
        return context.getContact()
    }

    private val newMessageFlow: MutableStateFlow<Pair<Contact, Message>?> = MutableStateFlow(null)
    private val sdfDay = SimpleDateFormat("DD")
    private var nowDay = sdfDay.format(now()).toInt()

    private val _chatinganQr by lazy {
        ChatinganQrImpl(getContact()) { contactPaired, qrImpl ->
            val contentContact = getContact().toJson()
            val existingContact = chatinganDao.getContactByEmail(contactPaired.email)
            if (existingContact != null) {
                val throwable = ChatinganException("Contact exist!")
                qrImpl.setPairListenerFailure(throwable)
                throw throwable
            }

            val sendData = messageEmitter.sendNotifier(
                contact = contactPaired,
                json = contentContact,
                notificationType = MessageNotifier.NotificationType.CONTACT_PAIR,
                messageType = Message.Type.OTHER
            )

            if (!sendData.isSuccess) {
                qrImpl.setPairListenerFailure(Throwable(sendData.message))
            } else {
                val contactEntity = DataMapper.mapContactToEntity(contactPaired)
                chatinganDao.insertContact(contactEntity)
                qrImpl.setPairListenerSuccess(contactPaired)
            }

            return@ChatinganQrImpl sendData.isSuccess
        }
    }

    override fun getChatinganQr(): ChatinganQr {
        return _chatinganQr
    }

    override fun bindToMessageSubscriber(
        messageNotifier: MessageNotifier,
        onMessageIncoming: (Contact, Message) -> Unit
    ) {
        when (messageNotifier.type) {
            MessageNotifier.NotificationType.MESSAGE -> {
                IOScope().launch {
                    saveMessageFromNotification(messageNotifier, onMessageIncoming)
                }
            }
            MessageNotifier.NotificationType.CONTACT_PAIR -> {
                IOScope().launch {
                    saveContactPairFromNotification(messageNotifier)
                }
            }
            MessageNotifier.NotificationType.TYPING -> {
                IOScope().launch {
                    updateContactTyping(messageNotifier)
                }
            }
            MessageNotifier.NotificationType.MESSAGE_REPORT -> {
                IOScope().launch {
                    updateMessageStatus(messageNotifier)
                }
            }
            MessageNotifier.NotificationType.CONTACT_UPDATE -> {
                IOScope().launch {
                    updateContact(messageNotifier)
                }
            }
            else -> {}
        }
    }

    override fun updateToken(fcmToken: String) {
        val existingContact = ChatinganPreferences.read<Contact>(context, "contact") ?: return
        val updatedContact = existingContact.copy(token = fcmToken)
        val updatedContactEntity = DataMapper.mapContactToEntity(updatedContact)
        IOScope().launch {
            chatinganDao.getAllContact()
                .firstOrNull()
                ?.onEach {
                    val contact = DataMapper.mapEntityToContact(it)
                    messageEmitter.sendNotifier(
                        contact = contact,
                        json = updatedContactEntity.toJson(),
                        notificationType = MessageNotifier.NotificationType.CONTACT_UPDATE,
                        messageType = Message.Type.OTHER
                    )
                }
        }
    }

    override fun onMessageUpdate(contact: Contact, newMessage: suspend (Message?) -> Unit) {
        MainScope().launch {
            newMessageFlow
                .filterNotNull()
                .collect { (con, msg) ->
                    if (contact.id == con.id) {
                        newMessage.invoke(msg)
                    } else {
                        newMessageFlow.value = null
                    }
                }
        }
    }

    override suspend fun addContact(contact: Contact) {
        val entity = DataMapper.mapContactToEntity(contact)
        if (chatinganDao.isContactExist(entity.email)) throw ChatinganException("Contact exists")

        chatinganDao.insertContact(entity)
        messageEmitter.sendNotifier(
            contact = contact,
            json = getContact().toJson(),
            notificationType = MessageNotifier.NotificationType.CONTACT_PAIR,
            messageType = Message.Type.OTHER,
            title = getContact().name,
            subtitle = "Success add ${getContact().name}"
        )
    }

    override suspend fun pairContact(contact: Contact) {
        addContact(contact)
        _chatinganQr.setPairListenerSuccess(contact)
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

    override fun createNewTextMessage(
        contact: Contact,
        textBuilder: Message.MessageTextBuilder.() -> Unit
    ): Message.TextMessages {
        val messageTextBuilder = Message.MessageTextBuilder().apply(textBuilder)

        return Message.TextMessages(
            id = UUID.randomUUID().toString(),
            senderId = getContact().id,
            receiverId = contact.id,
            status = Message.Status.SENDING,
            messageBody = messageTextBuilder.message,
            date = Utils.now().toDate()
        )
    }

    override suspend fun createNewImageMessage(
        contact: Contact,
        imageBuilder: Message.MessageImageBuilder.() -> Unit
    ): Message.ImageMessages {
        val messageImageBuilder = Message.MessageImageBuilder().apply(imageBuilder)
        val file = messageImageBuilder.file ?: throw ChatinganException("Image failure!")

        val uploadImageResult = imageUploader.upload(file)
        val uploadImage = uploadImageResult.getOrThrow()

        val messageImageBody = Message.MessageImageBody(
            imageUrl = uploadImage.imageUrl,
            thumbUrl = uploadImage.thumbUrl,
            caption = messageImageBuilder.caption
        )

        return Message.ImageMessages(
            id = UUID.randomUUID().toString(),
            senderId = getContact().id,
            receiverId = contact.id,
            status = Message.Status.SENDING,
            imageBody = messageImageBody,
            date = Utils.now().toDate()
        )
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
                .sortedByDescending {
                    it.lastMessage.superDate
                }
        }
    }

    override suspend fun sendMessage(contact: Contact, message: Message) {
        newMessageFlow.value = Pair(contact, message)
        when (message) {
            is Message.TextMessages -> sendTextMessage(contact, message)
            is Message.ImageMessages -> sendImageMessage(contact, message)
            else -> {}
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
                enablePlaceholders = false
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
                    if (withDivider) {
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
                    }
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
        messageEmitter.sendNotifier(
            contact = contact,
            json = messageReport.toJson(),
            notificationType = MessageNotifier.NotificationType.MESSAGE_REPORT,
            messageType = Message.Type.OTHER
        )
        chatinganDao.updateMessageStatus(messageReport.messageId, messageReport.status.name)
    }

    override suspend fun setTyping(contact: Contact, isTyping: Boolean) {
        val contactEntity = DataMapper.mapContactToEntity(getContact())
            .setTyping(isTyping)

        val content = contactEntity.toJson()
        messageEmitter.sendNotifier(
            contact = contact,
            json = content,
            notificationType = MessageNotifier.NotificationType.TYPING,
            messageType = Message.Type.OTHER
        )
    }

    private suspend fun sendTextMessage(contact: Contact, textMessages: Message.TextMessages) {
        if (textMessages.messageBody.isEmpty()) return
        val messageJson = textMessages.toJson()
        sendRawMessage(
            contact,
            textMessages,
            messageJson,
            textMessages.messageBody
        )
    }

    private suspend fun sendImageMessage(contact: Contact, imageMessages: Message.ImageMessages) {
        if (imageMessages.imageBody.imageUrl.isEmpty()) return
        val messageJson = imageMessages.toJson()
        sendRawMessage(contact, imageMessages, messageJson, "Image")
    }

    private suspend fun sendRawMessage(
        contact: Contact,
        message: Message,
        json: String,
        subtitle: String
    ) {
        val messageEntity = DataMapper.mapMessageToEntity(message)
        chatinganDao.insertMessage(messageEntity)

        val receiver = contact.copy(lastMessageId = messageEntity.id)
        val sendData = messageEmitter.sendNotifier(
            contact = receiver,
            json = json,
            notificationType = MessageNotifier.NotificationType.MESSAGE,
            messageType = Message.Type.valueOf(messageEntity.type.uppercase()),
            title = receiver.name,
            subtitle = subtitle
        )

        val messageStatus = if (sendData.isSuccess) {
            Message.Status.SENT
        } else {
            Message.Status.FAILURE
        }

        val newMessageEntity = messageEntity.changeStatus(messageStatus)
        val updatedContactEntity = DataMapper.mapContactToEntity(receiver)
        chatinganDao.updateContact(updatedContactEntity)

        val updatedContactMe = getContact().copy(lastMessageId = newMessageEntity.id)
        Chatingan.updateContact(context, updatedContactMe)

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
                saveRawMessageFromNotification(textMessage, onMessageIncoming)
            }
            Message.Type.IMAGE -> {
                val imageMessage: Message.ImageMessages = Utils.convertFromJson(notification.body)
                val newTextMessage = imageMessage.updateStatus(Message.Status.RECEIVED)
                val textMessageEntity = DataMapper.mapMessageToEntity(newTextMessage)
                chatinganDao.insertMessage(textMessageEntity)
                saveRawMessageFromNotification(imageMessage, onMessageIncoming)
            }
            else -> {

            }
        }
    }

    private suspend fun saveRawMessageFromNotification(
        message: Message,
        onMessageIncoming: (Contact, Message) -> Unit
    ) {
        val contactEntity = chatinganDao.getContactById(message.getChildSenderId())
            ?: throw ChatinganException("Contact not found!")
        val contact = DataMapper.mapEntityToContact(contactEntity)
            .copy(lastMessageId = message.getChildId())
        val newContactEntity = DataMapper.mapContactToEntity(contact)

        val messageReport = MessageReport(message.getChildId(), Message.Status.RECEIVED)
        messageEmitter.sendNotifier(
            contact = contact,
            json = messageReport.toJson(),
            notificationType = MessageNotifier.NotificationType.MESSAGE_REPORT,
            messageType = Message.Type.OTHER
        )
        chatinganDao.updateContact(newContactEntity)
        sendToServices(message.getChildId(), onMessageIncoming)

        newMessageFlow.value = Pair(contact, message)
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
        val messageEntity = chatinganDao.getMessageById(messageReport.messageId)

        if (messageEntity != null) {
            chatinganDao.updateMessageStatus(messageReport.messageId, messageReport.status.name)

            val senderId = messageEntity.senderId
            val contactEntity = if (senderId != getContact().id) {
                chatinganDao.getContactById(senderId)
            } else {
                chatinganDao.getContactById(messageEntity.receiverId)
            }
        }
    }

    private suspend fun updateContact(notification: MessageNotifier) {
        if (notification.type != MessageNotifier.NotificationType.CONTACT_UPDATE) return
        val contactEntity: ContactEntity = Utils.convertFromJson(notification.body)
        chatinganDao.updateContact(contactEntity)
    }

    private suspend fun setMessageFlow(contact: Contact, message: Message) {
        val pair = Pair(contact, message)
        val current = newMessageFlow.value
        if (current != pair) {
            newMessageFlow.emit(pair)
        }
    }

    /*private suspend fun sendNotification(
        token: String,
        json: String,
        notificationType: MessageNotifier.NotificationType,
        messageType: Message.Type,
        title: String = "",
        subtitle: String = ""
    ): NotifierResult {
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

        val response = firebaseWebServices.sendMessage(firebaseRequest)
        if (!response.isSuccessful) return NotifierResult(false, "Internal failure")

        val responseBody = response.body()
        val errorResult = responseBody?.results?.firstOrNull()
            ?.error

        val isSuccess = response.code() == 200 && errorResult == null
        val message = errorResult ?: "Success"

        return NotifierResult(isSuccess, message)
    }*/

    companion object {
        private const val ELLIPSIZE_MAX = 20
    }

}