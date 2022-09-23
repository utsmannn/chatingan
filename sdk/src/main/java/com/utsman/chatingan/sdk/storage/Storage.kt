package com.utsman.chatingan.sdk.storage

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.utsman.chatingan.common.event.FlowEvent
import com.utsman.chatingan.common.event.StateEvent
import com.utsman.chatingan.common.event.defaultStateEvent
import com.utsman.chatingan.common.event.map
import com.utsman.chatingan.sdk.data.entity.Chat
import com.utsman.chatingan.sdk.data.type.Entity
import com.utsman.chatingan.sdk.data.type.Store
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.*
import kotlin.coroutines.resume

abstract class Storage<T : Store, U : Entity> {
    private val firebaseFirestore: FirebaseFirestore by lazy {
        Firebase.firestore
    }

    abstract fun path(): String
    abstract fun dateField(): String
    abstract fun mapStoreTransform(map: Map<String, Any>, date: Date): T
    abstract fun dataMapper(store: T): U

    private fun collection(): CollectionReference {
        return firebaseFirestore.collection(path())
    }

    fun addItem(item: T, id: String, additionalData: Any? = null, isMerge: Boolean = true): FlowEvent<U> {
        println("ASUUUU => document add -> $id")
        val addItemState = defaultStateEvent<U>()
        if (id.isNotEmpty()) {
            collection()
                .document(id)
                .run {
                    if (isMerge) {
                        set(item, SetOptions.merge())
                    } else {
                        set(item)
                    }
                }
                .addOnSuccessListener {
                    val data = dataMapper(item)
                    val successState = StateEvent.Success(data)
                    addItemState.value = successState

                    if (additionalData != null) {
                        updateAdditionalData(additionalData)
                    }
                }
                .addOnFailureListener {
                    val failureState = StateEvent.Failure<U>(it)
                    addItemState.value = failureState
                }
        } else {
            addItemState.value = StateEvent.Empty()
        }

        return addItemState
    }

    fun <S>updateAdditionalData(additionalData: S): FlowEvent<S> {
        val addAdditionalState = defaultStateEvent<S>()
        if (additionalData != null) {
            collection()
                .parent
                ?.set(additionalData, SetOptions.merge())
                ?.addOnSuccessListener {
                    val successState = StateEvent.Success<S>(additionalData)
                    addAdditionalState.value = successState
                }
                ?.addOnFailureListener {
                    val failureState = StateEvent.Failure<S>(it)
                    addAdditionalState.value = failureState
                }
        }

        return addAdditionalState
    }

    open fun <S : Any> listenAdditionalItem(mapper: (map: Map<String, Any>, date: Date) -> S): FlowEvent<S> {
        val additionalState = defaultStateEvent<S>()
        collection()
            .parent
            ?.addSnapshotListener { value, error ->
                if (error != null) {
                    val failureState = StateEvent.Failure<S>(error)
                    additionalState.value = failureState
                }

                if (value != null) {
                    val rawData = value.data
                    val date = value.getDate(dateField())

                    if (rawData != null && date != null) {
                        val data = mapper.invoke(rawData, date)
                        val successState = StateEvent.Success(data)
                        additionalState.value = successState
                    }
                } else {
                    val emptyState = StateEvent.Empty<S>()
                    additionalState.value = emptyState
                }
            }

        return additionalState
    }

    open fun listenItem(): FlowEvent<List<U>> {
        val listState = defaultStateEvent<List<U>>()
        collection()
            .orderBy(FIELD_LAST_UPDATE)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    val failureState = StateEvent.Failure<List<U>>(error)
                    listState.value = failureState
                }

                if (value != null) {
                    val contacts = value.documents
                        .map {
                            Pair(it.data, it.getDate(dateField()))
                        }
                        .filter { it.first != null && it.second != null }
                        .map {
                            mapStoreTransform(checkNotNull(it.first), checkNotNull(it.second))
                        }
                        .map { dataMapper(it) }

                    val successState = StateEvent.Success(contacts)
                    listState.value = successState
                } else {
                    val emptyState = StateEvent.Empty<List<U>>()
                    listState.value = emptyState
                }
            }

        return listState
    }

    suspend fun findItemStoreById(id: String): T? {
        return suspendCancellableCoroutine { task ->
            collection()
                .document(id)
                .get()
                .addOnSuccessListener {
                    val dataRaw = it.data
                    val date = it.getDate(dateField())
                    if (dataRaw != null && date != null) {
                        val data = mapStoreTransform(dataRaw, date)
                        task.resume(data)
                    } else {
                        task.resume(null)
                    }
                }
                .addOnFailureListener {
                    task.resume(null)
                }
                .addOnFailureListener {
                    task.resume(null)
                }
        }
    }

    suspend fun findItemStoreByIdFlow(id: String): Flow<StateEvent<T>> {
        return flow {
            val loadingState = StateEvent.Loading<T>()
            emit(loadingState)
            val data = findItemStoreById(id)
            val finalState = if (data != null) {
                StateEvent.Success(data)
            } else {
                StateEvent.Failure(Throwable("Empty"))
            }
            emit(finalState)
        }
    }

    suspend fun findItemById(id: String): U? {
        val store = findItemStoreById(id) ?: return null
        return dataMapper(store)
    }

    suspend fun findItemByIdFlow(id: String): Flow<StateEvent<U>> {
        val store = findItemStoreByIdFlow(id).map { state ->
            state.map { data ->
                dataMapper(data)
            }
        }
        return store
    }

    companion object {
        internal const val FIELD_LAST_UPDATE = "lastUpdate"
    }
}