package com.utsman.chatingan.sdk.storage

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.utsman.chatingan.common.IOScope
import com.utsman.chatingan.common.event.FlowEvent
import com.utsman.chatingan.common.event.StateEvent
import com.utsman.chatingan.common.event.defaultStateEvent
import com.utsman.chatingan.common.event.invoke
import com.utsman.chatingan.common.event.map
import com.utsman.chatingan.sdk.data.config.ChatinganConfig
import com.utsman.chatingan.sdk.data.entity.Chat
import com.utsman.chatingan.sdk.data.entity.ChatInfo
import com.utsman.chatingan.sdk.data.entity.Contact
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class ChatStorage(
    private val contactStorage: ContactStorage,
    private val config: ChatinganConfig
) {

    private val firebaseFirestore: FirebaseFirestore by lazy {
        Firebase.firestore
    }

    suspend fun getChatList(): FlowEvent<List<Chat>> {
        return getPaths()
            .map { state ->
                println("ASUUUU path state -> $state")
                state.map { list ->
                    println("ASUUUU -> list anu $list")
                    list.filter { it.isNotEmpty() }.mapNotNull {
                        getInfo(it)
                    }
                }
            }.stateIn(IOScope())
    }

    private suspend fun getInfo(path: String): Chat? {
        return suspendCancellableCoroutine { task ->
            firebaseFirestore
                .collection(COLLECTION_PATH)
                .document(path)
                .get()
                .addOnSuccessListener { value ->
                    val data = value?.data
                    println("ASUUUUUUUUUU -> data found -> $data")

                    if (data != null) {
                        IOScope().launch {
                            val contacts = path.split("_").mapNotNull {
                                contactStorage.findItemById(it)
                            }
                            val chatInfo = ChatInfo(
                                lastMessage = data["lastMessage"].toString()
                            )
                            println("ASUUUUU => contact di mari -> $contacts")
                            val chat = Chat(
                                id = path,
                                contacts = contacts,
                                chatInfo = chatInfo
                            )
                            task.resume(chat)
                        }
                    } else {
                        task.resume(null)
                    }
                }
                .addOnFailureListener { error ->
                    task.resume(null)
                }
        }
    }

    private fun getPaths(): FlowEvent<List<String>> {
        val pathState = defaultStateEvent<List<String>>()
        firebaseFirestore
            .collection(COLLECTION_PATH)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    pathState.value = StateEvent.Failure(error)
                } else {
                    val paths = value?.documents?.map {
                        it.id
                    }.orEmpty()
                        .filter { it.split("_").count() == 2 }

                    println("ASUUUUU -> path $paths")
                    println("ASUUUUU -> path count ${paths.count()}")

                    if (paths.isNotEmpty()) {
                        println("ASUUUUU -> paths")
                        println(paths.toString())
                        println("ASUUUUU -> paths")

                        pathState.value = StateEvent.Success(paths)
                    } else {
                        pathState.value = StateEvent.Empty()
                    }
                }
            }
        return pathState
    }

    companion object {
        private const val COLLECTION_PATH = "chats"
    }

}