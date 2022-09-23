package com.utsman.chatingan.sdk.storage

import com.utsman.chatingan.sdk.data.entity.Chat
import com.utsman.chatingan.sdk.data.entity.ChatInfo
import com.utsman.chatingan.sdk.data.entity.Contact
import com.utsman.chatingan.sdk.data.store.ChatInfoStore
import java.util.*

class ChatInfoStorage(
    private val contacts: List<Contact>
) : Storage<ChatInfoStore, ChatInfo>() {
    override fun path(): String {
        return "chats" + "/" + Chat.generateId(contacts) + "/messages"
    }

    override fun dateField(): String {
        return FIELD_LAST_UPDATE
    }

    override fun mapStoreTransform(map: Map<String, Any>, date: Date): ChatInfoStore {
        return ChatInfoStore.fromMap(map, date)
    }

    override fun dataMapper(store: ChatInfoStore): ChatInfo {
        return store.toChatInfo()
    }

    companion object {
        private const val COLLECTION_PATH = "chat_info"
    }
}