package com.utsman.chatingan.sdk.storage

import com.utsman.chatingan.sdk.data.entity.MessageChat
import com.utsman.chatingan.sdk.data.store.MessageChatStore
import java.util.*

class MessageChatStorage(
    val id: String
) : Storage<MessageChatStore, MessageChat>() {

    override fun path(): String {
        return "chats/$id/messages"
    }

    override fun mapStoreTransform(map: Map<String, Any>, date: Date): MessageChatStore {
        return MessageChatStore.fromMap(map, date)
    }

    override fun dataMapper(store: MessageChatStore): MessageChat {
        return store.toMessageChat()
    }

    override fun dateField(): String {
        return FIELD_LAST_UPDATE
    }
}