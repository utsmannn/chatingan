package com.utsman.chatingan.sdk.database

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.utsman.chatingan.common.event.FlowEvent
import com.utsman.chatingan.common.event.StateEvent
import com.utsman.chatingan.common.event.loadingStateEvent
import com.utsman.chatingan.sdk.data.config.ChatinganConfig
import com.utsman.chatingan.sdk.data.entity.ChatInfo
import com.utsman.chatingan.sdk.data.entity.Contact
import com.utsman.chatingan.sdk.data.store.ChatInfoStore
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class ChatInfoDatabase : RemoteDatabase<ChatInfoStore, ChatInfo>(ChatInfoStore::class) {
    override fun path(): String {
        return COLLECTION_PATH
    }

    override fun dataMapper(store: ChatInfoStore): ChatInfo {
        return store.toChatInfo()
    }

    class IdFinder(private val config: ChatinganConfig) {
        private val firebaseFirestore: FirebaseFirestore by lazy {
            Firebase.firestore
        }

        private fun meContactId(): String = config.contact.id

        fun listenForContact(contact: Contact): FlowEvent<String> {
            val statePath = loadingStateEvent<String>()
            firebaseFirestore
                .collection(COLLECTION_PATH)
                .whereEqualTo("memberIds", arrayListOf(meContactId(), contact.id).sorted())
                .addSnapshotListener { value, error ->
                    if (error != null) {
                        statePath.value = StateEvent.Failure(error)
                    } else {
                        val document = value?.documents?.firstOrNull()
                        if (document != null) {
                            val docId = document.id
                            statePath.value = StateEvent.Success(docId)
                        } else {
                            statePath.value = StateEvent.Empty()
                        }
                    }
                }

            return statePath
        }

        suspend fun getForContact(contact: Contact): String? {
            return suspendCancellableCoroutine { task ->
                firebaseFirestore
                    .collection(COLLECTION_PATH)
                    .whereEqualTo("memberIds", arrayListOf(meContactId(), contact.id).sorted())
                    .get()
                    .addOnSuccessListener { value ->
                        val document = value?.documents?.firstOrNull()
                        if (document != null) {
                            val docId = document.id
                            task.resume(docId)
                        } else {
                            task.resume(null)
                        }
                    }
                    .addOnFailureListener { error ->
                        error.printStackTrace()
                        task.resume(null)
                    }
            }
        }
    }


    companion object {
        private const val COLLECTION_PATH = "chat_info"
    }

}