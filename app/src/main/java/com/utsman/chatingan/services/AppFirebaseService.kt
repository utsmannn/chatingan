package com.utsman.chatingan.services

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class AppFirebaseService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        println("------- message incoming ------")
        println(message.data)
        println("------- message incoming end ------")
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }

}