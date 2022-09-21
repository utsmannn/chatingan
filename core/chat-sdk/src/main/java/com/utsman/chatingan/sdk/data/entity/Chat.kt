package com.utsman.chatingan.sdk.data.entity

data class Chat(
    var id: String = "",
    var contacts: List<Contact> = emptyList(),
    var messages: List<MessageChat> = emptyList()
) {

    init {
        id = generateId(contacts)
    }

    companion object {
        fun generateId(contacts: List<Contact>): String {
            return contacts.sortedBy { it.id }
                .map { it.id }
                .joinToString { it }
        }
    }
}