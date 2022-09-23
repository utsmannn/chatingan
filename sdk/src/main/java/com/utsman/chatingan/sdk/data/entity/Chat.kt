package com.utsman.chatingan.sdk.data.entity

data class Chat(
    var id: String = "",
    var contacts: List<Contact> = emptyList(),
    var messages: List<MessageChat> = emptyList(),
    var chatInfo: ChatInfo = ChatInfo()
) {

    init {
        id = generateId(contacts)
    }

    companion object {
        fun generateId(contacts: List<Contact>): String {
            return contacts.sortedBy { it.id }.joinToString("_") { it.id }
        }
    }
}