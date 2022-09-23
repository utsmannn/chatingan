package com.utsman.chatingan.services

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.utsman.chatingan.common.IOScope
import com.utsman.chatingan.sdk.Chatingan
import kotlinx.coroutines.launch

class AppFirebaseService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        println("------- message incoming ------")
        println(message.data)
        println("------- message incoming end ------")
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        println("ASUUU new token fcm ---")
        println(token)
        println("ASUUU new token fcm --- end ")
        IOScope().launch {
            Chatingan.getInstance().updateFcm(token)
        }
    }

}