package com.utsman.chatingan.sdk.database

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.utsman.chatingan.common.event.FlowEvent
import com.utsman.chatingan.common.event.StateEvent
import com.utsman.chatingan.common.event.defaultStateEvent
import com.utsman.chatingan.common.event.loadingStateEvent
import com.utsman.chatingan.common.event.map
import com.utsman.chatingan.sdk.data.contract.Entity
import com.utsman.chatingan.sdk.data.contract.Store
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.*
import kotlin.coroutines.resume
import kotlin.reflect.KClass

abstract class RemoteDatabase<T : Store, U : Entity>(private val storeClass: KClass<T>) {
    private val firebaseFirestore: FirebaseFirestore by lazy {
        Firebase.firestore
    }

    abstract fun path(): String
    abstract fun dataMapper(store: T): U

    private fun collection(): CollectionReference {
        return firebaseFirestore.collection(path())
    }

    open fun dateField(): String {
        return FIELD_LAST_UPDATE
    }

    fun addItem(item: T, id: String, additionalData: Any? = null, isMerge: Boolean = true): FlowEvent<U> {
        val addItemState = loadingStateEvent<U>()
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
        val additionalState = loadingStateEvent<S>()
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
        val listState = loadingStateEvent<List<U>>()
        collection()
            .orderBy(FIELD_LAST_UPDATE)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    val failureState = StateEvent.Failure<List<U>>(error)
                    listState.value = failureState
                }
                if (value != null) {
                    val items = value.documents.mapNotNull { it.toObject(storeClass.java) }
                        .map { dataMapper(it) }

                    val successState = StateEvent.Success(items)
                    listState.value = successState
                } else {
                    val emptyState = StateEvent.Empty<List<U>>()
                    listState.value = emptyState
                }
            }

        return listState
    }

    fun listenItemById(id: String): FlowEvent<U> {
        val itemState = loadingStateEvent<U>()
        collection()
            .document(id)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    itemState.value = StateEvent.Failure(error)
                }
                val item = value?.toObject(storeClass.java)

                if (item != null) {
                    itemState.value = StateEvent.Success(dataMapper(item))
                } else {
                    itemState.value = StateEvent.Empty()
                }
            }
        return itemState
    }

    private suspend fun findItemStoreById(id: String): T? {
        return suspendCancellableCoroutine { task ->
            collection()
                .document(id)
                .get()
                .addOnSuccessListener {
                    val data = it.toObject(storeClass.java)
                    if (data != null) {
                        task.resume(data)
                    } else {
                        task.resume(null)
                    }
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