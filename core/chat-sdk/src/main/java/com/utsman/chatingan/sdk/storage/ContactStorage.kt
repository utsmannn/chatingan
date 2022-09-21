package com.utsman.chatingan.sdk.storage

import com.utsman.chatingan.sdk.data.entity.Contact
import com.utsman.chatingan.sdk.data.store.ContactStore
import java.util.*

class ContactStorage : Storage<ContactStore, Contact>() {

    override fun path(): String {
        return COLLECTION_CONTACT
    }

    override fun mapStoreTransform(map: Map<String, Any>, date: Date): ContactStore {
        return ContactStore.fromMap(map, date)
    }

    override fun dataMapper(store: ContactStore): Contact {
        return store.toContact()
    }

    override fun dateField(): String {
        return ContactStore.FIELD_LAST_UPDATE
    }

    companion object {
        internal const val COLLECTION_CONTACT = "contacts"
    }
}