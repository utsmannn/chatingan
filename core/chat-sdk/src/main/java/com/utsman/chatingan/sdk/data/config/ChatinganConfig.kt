package com.utsman.chatingan.sdk.data.config

import com.utsman.chatingan.sdk.data.entity.Contact
import java.util.*

data class ChatinganConfig(
    var serverKey: String = "",
    var contact: Contact = Contact()
) {

    class ChatinganContactBuilder {
        private var name: String = ""
        private var id: String = UUID.randomUUID().toString()
        private var image: String = ""

        fun setId(id: String): ChatinganContactBuilder {
            this.id = id
            return this
        }

        fun setName(name: String): ChatinganContactBuilder {
            this.name = name
            return this
        }

        fun setImage(image: String): ChatinganContactBuilder {
            this.image = image
            return this
        }

        fun build(): Contact {
            return Contact(id, name, image)
        }
    }

    class ChatinganConfigBuilder {
        private var serverKey: String = ""
        private var contact: Contact = Contact()

        fun setServerKey(serverKey: String): ChatinganConfigBuilder {
            this.serverKey = serverKey
            return this
        }

        fun setContact(contact: Contact): ChatinganConfigBuilder {
            this.contact = contact
            return this
        }

        fun build(): ChatinganConfig {
            return ChatinganConfig(serverKey, contact)
        }
    }
}