package com.utsman.chatingan.sdk.storage

import com.utsman.chatingan.common.IOScope
import com.utsman.chatingan.common.event.FlowEvent
import com.utsman.chatingan.common.event.defaultStateEvent
import com.utsman.chatingan.common.event.map
import com.utsman.chatingan.sdk.data.entity.Chat
import com.utsman.chatingan.sdk.data.entity.Contact
import com.utsman.chatingan.sdk.data.entity.MessageChat
import com.utsman.chatingan.sdk.data.store.MessageChatStore
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.*

class ChatStorage(
    private val contacts: List<Contact>
) : Storage<MessageChatStore, MessageChat>() {

    private val _chat = defaultStateEvent<Chat>()

    override fun path(): String {
        return "chats" + "/" + Chat.generateId(contacts) + "/messages"
    }

    override fun mapStoreTransform(map: Map<String, Any>, date: Date): MessageChatStore {
        return MessageChatStore.fromMap(map, date)
    }

    override fun dataMapper(store: MessageChatStore): MessageChat {
        return store.toMessageChat()
    }

    override fun dateField(): String {
        return MessageChatStore.FIELD_LAST_UPDATE
    }

    suspend fun getChat(): FlowEvent<Chat> {
        return listenItem()
            .map {
                println("ASUUUU get chat storage $it..")
                it.map { messages ->
                    Chat(
                        contacts = contacts,
                        messages = messages
                    )
                }
            }.stateIn(IOScope())

        //return _chat
    }
}