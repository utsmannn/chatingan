package com.utsman.chatingan.sdk.storage

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.utsman.chatingan.common.event.FlowEvent
import com.utsman.chatingan.common.event.StateEvent
import com.utsman.chatingan.common.event.defaultStateEvent
import com.utsman.chatingan.common.event.map
import com.utsman.chatingan.sdk.data.type.Entity
import com.utsman.chatingan.sdk.data.type.Store
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.*
import kotlin.coroutines.resume

abstract class Storage<T : Store, U : Entity> {
    private val db: FirebaseFirestore by lazy {
        Firebase.firestore
    }

    private val _addItemState = defaultStateEvent<U>()
    private val _addItemInCollectionState = defaultStateEvent<U>()
    private val _listState = defaultStateEvent<List<U>>()

    abstract fun path(): String
    abstract fun dateField(): String
    abstract fun mapStoreTransform(map: Map<String, Any>, date: Date): T
    abstract fun dataMapper(store: T): U

    private fun collection(): CollectionReference {
        return db.collection(path())
    }

    suspend fun addItem(item: T, id: String): FlowEvent<U> {
        if (findItemStoreById(id) == null) {
            collection()
                .document(id)
                .set(item)
                .addOnSuccessListener {
                    val data = dataMapper(item)
                    val successState = StateEvent.Success(data)
                    _addItemState.value = successState
                }
                .addOnFailureListener {
                    val failureState = StateEvent.Failure<U>(it)
                    _addItemState.value = failureState
                }
        }

        return _addItemState
    }

    open fun listenItem(): FlowEvent<List<U>> {
        collection()
            .addSnapshotListener { value, error ->
                if (error != null) {
                    val failureState = StateEvent.Failure<List<U>>(error)
                    _listState.value = failureState
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
                    _listState.value = successState
                } else {
                    val emptyState = StateEvent.Empty<List<U>>()
                    _listState.value = emptyState
                }
            }

        return _listState
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
}