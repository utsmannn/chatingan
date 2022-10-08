package com.utsman.chatingan.lib

import com.utsman.chatingan.lib.data.model.Contact

data class ChatinganConfiguration(
    val contact: Contact,
    val fcmToken: String,
    val fcmServerKey: String
)