package com.utsman.chatingan.services

import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.utsman.chatingan.R
import com.utsman.chatingan.lib.Chatingan
import com.utsman.chatingan.lib.calculateIntId
import com.utsman.chatingan.lib.ifTextMessage

class AppFirebaseService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        Chatingan.getInstance().bindToFirebaseMessagingServices(message.data) { contact, msg ->
            msg.ifTextMessage {
                val builder = NotificationCompat.Builder(this@AppFirebaseService, "chatingan-anu")
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentTitle(contact.name)
                    .setContentText(it.messageBody)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)

                val notificationId = msg.getChildId().calculateIntId()
                NotificationManagerCompat.from(this@AppFirebaseService)
                    .notify(notificationId, builder.build())
            }
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        println("new token fcm ---")
        println(token)
        println("new token fcm --- end ")
        Chatingan.getInstance().updateFcmToken(token)
    }

}