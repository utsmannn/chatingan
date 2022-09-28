package com.utsman.chatingan.common.event

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.utsman.chatingan.common.ui.EmptyScreen
import com.utsman.chatingan.common.ui.FailureScreen
import com.utsman.chatingan.common.ui.LoadingScreen
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

fun <T> defaultStateEvent(): MutableStateFlow<StateEvent<T>> = MutableStateFlow(StateEvent.Idle())
fun <T> loadingStateEvent(): MutableStateFlow<StateEvent<T>> =
    MutableStateFlow(StateEvent.Loading())

fun <T> emptyStateEvent(): MutableStateFlow<StateEvent<T>> = MutableStateFlow(StateEvent.Empty())
fun <T> errorStateEvent(message: String?): MutableStateFlow<StateEvent<T>> = MutableStateFlow(
    StateEvent.Failure(
        Throwable(message)
    )
)

fun <T> loadingEventValue() = StateEvent.Loading<T>()

inline fun <T, U> StateEvent<T>.map(mapper: (T) -> U): StateEvent<U> {
    return when (this) {
        is StateEvent.Idle -> StateEvent.Idle()
        is StateEvent.Loading -> StateEvent.Loading()
        is StateEvent.Failure -> StateEvent.Failure(exception)
        is StateEvent.Success -> StateEvent.Success(mapper(data))
        is StateEvent.Empty -> StateEvent.Empty()
    }
}

inline fun <reified T> FlowEvent<T>.subscribeStateOf(
    activity: ComponentActivity,
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    crossinline onIdle: () -> Unit = {},
    crossinline onLoading: () -> Unit = {},
    crossinline onFailure: Throwable.() -> Unit = {},
    crossinline onEmpty: () -> Unit = {},
    crossinline onSuccess: T.() -> Unit = {}
) {
    asLiveData(coroutineContext).observe(activity) {
        when (it) {
            is StateEvent.Idle -> onIdle.invoke()
            is StateEvent.Loading -> onLoading.invoke()
            is StateEvent.Failure -> onFailure.invoke(it.exception)
            is StateEvent.Success -> onSuccess.invoke(it.data)
            is StateEvent.Empty -> onEmpty.invoke()
        }
    }
}

inline fun <reified T> LiveData<StateEvent<T>>.subscribeStateOf(
    activity: ComponentActivity,
    crossinline onIdle: () -> Unit = {},
    crossinline onLoading: () -> Unit = {},
    crossinline onFailure: Throwable.() -> Unit = {},
    crossinline onEmpty: () -> Unit = {},
    crossinline onSuccess: T.() -> Unit
) {
    this.observe(activity) {
        when (it) {
            is StateEvent.Idle<*> -> onIdle.invoke()
            is StateEvent.Loading<*> -> onLoading.invoke()
            is StateEvent.Failure<*> -> onFailure.invoke(it.exception)
            is StateEvent.Success<*> -> onSuccess.invoke(it.data as T)
            is StateEvent.Empty<*> -> onEmpty.invoke()
        }
    }
}

@Composable
inline fun <T> FlowEvent<T>.composeStateOf(
    onIdle: @Composable () -> Unit = { EmptyScreen() },
    onLoading: @Composable () -> Unit = { LoadingScreen() },
    onFailure: @Composable Throwable.() -> Unit = { FailureScreen(message = message.orEmpty()) },
    onEmpty: @Composable () -> Unit = { EmptyScreen() },
    onSuccess: @Composable T.() -> Unit
) {
    when (val state = this.distinctUntilChanged { old, new ->
        old.invoke() == new.invoke()
    }.collectAsState(StateEvent.Idle()).value) {
        is StateEvent.Idle -> onIdle.invoke()
        is StateEvent.Loading -> onLoading.invoke()
        is StateEvent.Failure -> onFailure.invoke(state.exception)
        is StateEvent.Success -> onSuccess.invoke(state.data)
        is StateEvent.Empty -> onEmpty.invoke()
    }
}

@Composable
inline fun <T, U> FlowEvent<T>.composeStateOfMerge(
    other: FlowEvent<U>,
    onIdle: @Composable () -> Unit = { EmptyScreen() },
    onLoading: @Composable () -> Unit = { LoadingScreen() },
    onFailure: @Composable Throwable.() -> Unit = { FailureScreen(message = message.orEmpty()) },
    onEmpty: @Composable () -> Unit = { EmptyScreen() },
    onSuccess: @Composable (T, U) -> Unit
) {
    when (val state = collectAsState().value) {
        is StateEvent.Idle -> onIdle.invoke()
        is StateEvent.Loading -> onLoading.invoke()
        is StateEvent.Failure -> onFailure.invoke(state.exception)
        is StateEvent.Success -> {
            other.composeStateOf(
                onIdle = onIdle,
                onLoading = onLoading,
                onFailure = onFailure,
                onEmpty = onEmpty,
                onSuccess = {
                    onSuccess.invoke(state.data, this)
                }
            )
        }
        is StateEvent.Empty -> onEmpty.invoke()
    }
}

suspend inline fun <reified T> FlowEvent<T>.subscribeStateOf(
    crossinline onIdle: () -> Unit = {},
    crossinline onLoading: () -> Unit = {},
    crossinline onFailure: Throwable.() -> Unit = {},
    crossinline onEmpty: () -> Unit = {},
    crossinline onSuccess: T.() -> Unit = {}
) {
    collect { state ->
        when (state) {
            is StateEvent.Idle -> onIdle.invoke()
            is StateEvent.Loading -> onLoading.invoke()
            is StateEvent.Failure -> onFailure.invoke(state.exception)
            is StateEvent.Success -> onSuccess.invoke(state.data)
            is StateEvent.Empty -> onEmpty.invoke()
        }
    }
}

inline fun <reified T> StateEvent<T>.onSuccess(data: (T) -> Unit) {
    if (this is StateEvent.Success) {
        data.invoke(this.data)
    }
}

inline fun <reified T> StateEvent<T>.doOnIdle(action: () -> Unit): StateEvent<T> {
    if (this is StateEvent.Idle) {
        action.invoke()
    }
    return this
}

inline fun <reified T> StateEvent<T>.doOnLoading(action: () -> Unit): StateEvent<T> {
    if (this is StateEvent.Loading) {
        action.invoke()
    }
    return this
}

inline fun <reified T> StateEvent<T>.doOnSuccess(data: (T) -> Unit): StateEvent<T> {
    if (this is StateEvent.Success) {
        data.invoke(this.data)
    }
    return this
}

inline fun <reified T> StateEvent<T>.doOnFailure(failure: (Throwable) -> Unit): StateEvent<T> {
    if (this is StateEvent.Failure) {
        failure.invoke(this.exception)
    }
    return this
}

inline fun <reified T> StateEvent<T>.doOnEmpty(failure: () -> Unit): StateEvent<T> {
    if (this is StateEvent.Empty) {
        failure.invoke()
    }
    return this
}

@Composable
inline fun <reified T> StateEvent<T>.defaultCompose(): StateEvent<T> {
    return this
        .doOnFailure { FailureScreen(message = it.message.orEmpty()) }
        .doOnLoading { LoadingScreen() }
        .doOnEmpty { EmptyScreen() }
}

operator fun <T> StateEvent<T>.invoke(): T? {
    return if (this is StateEvent.Success) {
        data
    } else {
        null
    }
}

fun <T> StateEvent<T>.getExceptionOfNull(): Throwable? {
    return if (this is StateEvent.Failure) {
        exception
    } else {
        null
    }
}

suspend fun <T> FlowEvent<List<T>>.filterFlow(predicate: (T) -> Boolean): FlowEvent<List<T>> {
    val filtered = map {
        if (it is StateEvent.Success) {
            val data = it.data.filter(predicate)
            StateEvent.Success(data)
        } else {
            it
        }
    }
    return filtered.stateIn(MainScope())
}


suspend fun <T> Flow<StateEvent<List<T>>>.filterFlow(predicate: (T) -> Boolean): FlowEvent<List<T>> {
    val filtered = map {
        if (it is StateEvent.Success) {
            val data = it.data.filter(predicate)
            StateEvent.Success(data)
        } else {
            it
        }
    }
    return filtered.stateIn(MainScope())
}


