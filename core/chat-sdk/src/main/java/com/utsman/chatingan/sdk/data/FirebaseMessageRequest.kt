package com.utsman.chatingan.sdk.data

data class FirebaseMessageRequest(
    var to: String,
    var data: Notification
) {
    data class Notification(
        var body: String,
        var title: String,
        var subtitle: String,
        var time: String = System.currentTimeMillis().toString()
    )
}