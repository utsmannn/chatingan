package com.utsman.chatingan.sdk

import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

object ChatinganFirebase {
    fun init() {
        FirebaseMessaging.getInstance().isAutoInitEnabled = true
    }

    suspend fun fcmToken(): String = withContext(Dispatchers.IO) {
        suspendCancellableCoroutine {
            FirebaseMessaging.getInstance().token
                .addOnCompleteListener { taskFcm ->
                    if (!taskFcm.isSuccessful) {
                        it.cancel()
                    }

                    it.resume(taskFcm.result)
                }
        }
    }
}