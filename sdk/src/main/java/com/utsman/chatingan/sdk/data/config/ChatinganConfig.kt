package com.utsman.chatingan.sdk.data.config

import com.utsman.chatingan.sdk.data.entity.Contact
import java.util.*

data class ChatinganConfig(
    var serverKey: String = "",
    var fcmToken: String = "",
    var contact: Contact = Contact()
) {

    class ChatinganContactBuilder {
        private var name: String = ""
        private var id: String = UUID.randomUUID().toString()
        private var image: String = ""
        private var detail: Contact.Detail = Contact.Detail()

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

        fun setDetail(detail: Contact.Detail): ChatinganContactBuilder {
            this.detail = detail
            return this
        }

        fun build(): Contact {
            return Contact(
                id, name, image, detail
            )
        }
    }

    class ChatinganConfigBuilder {
        private var serverKey: String = ""
        private var fcmToken: String = ""
        private var contact: Contact = Contact()

        fun setServerKey(serverKey: String): ChatinganConfigBuilder {
            this.serverKey = serverKey
            return this
        }

        fun setContact(contact: Contact): ChatinganConfigBuilder {
            this.contact = contact
            return this
        }

        fun setFcmToken(fcmToken: String): ChatinganConfigBuilder {
            this.fcmToken = fcmToken
            return this
        }

        fun build(): ChatinganConfig {
            return ChatinganConfig(serverKey, fcmToken, contact)
        }
    }
}