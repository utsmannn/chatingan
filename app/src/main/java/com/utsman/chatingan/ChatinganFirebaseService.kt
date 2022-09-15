package com.utsman.chatingan

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class ChatinganFirebaseService : FirebaseMessagingService() {

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