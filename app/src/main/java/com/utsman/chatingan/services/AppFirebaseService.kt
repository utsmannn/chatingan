package com.utsman.chatingan.services

import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.utsman.chatingan.R
import com.utsman.chatingan.lib.Chatingan
import com.utsman.chatingan.lib.calculateIntId
import com.utsman.chatingan.lib.ifImageMessage
import com.utsman.chatingan.lib.ifTextMessage
import com.utsman.chatingan.lib.receiver.MessageNotifier

class AppFirebaseService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val notifier = MessageNotifier.fromMap(message.data)
        Chatingan.getInstance().bindToMessageSubscriber(notifier) { contact, msg ->
            val builder = NotificationCompat.Builder(this@AppFirebaseService, "chatingan-anu")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(contact.name)
                .setPriority(NotificationCompat.PRIORITY_HIGH)

            msg.ifTextMessage {
                builder.setContentText(it.messageBody)
            }
            msg.ifImageMessage {
                builder.setContentText("[image]")
            }

            val notificationId = msg.getChildId().calculateIntId()
            NotificationManagerCompat.from(this@AppFirebaseService)
                .notify(notificationId, builder.build())
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