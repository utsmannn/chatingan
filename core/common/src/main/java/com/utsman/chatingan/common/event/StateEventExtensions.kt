package com.utsman.chatingan.common.event

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.utsman.chatingan.common.ui.Empty
import com.utsman.chatingan.common.ui.Failure
import com.utsman.chatingan.common.ui.Loading
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

fun <T> defaultStateEvent(): MutableStateFlow<StateEvent<T>> = MutableStateFlow(StateEvent.Idle())

inline fun <reified T, reified U> StateEvent<T>.map(mapper: (T) -> U): StateEvent<U> {
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
    crossinline onSuccess: T.() -> Unit
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
        println("")
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
    onIdle: @Composable () -> Unit = { Empty() },
    onLoading: @Composable () -> Unit = { Loading() },
    onFailure: @Composable Throwable.() -> Unit = { Failure(message = message.orEmpty()) },
    onEmpty: @Composable () -> Unit = { Empty() },
    onSuccess: @Composable T.() -> Unit
) {
    when (val state = collectAsState().value) {
        is StateEvent.Idle -> onIdle.invoke()
        is StateEvent.Loading -> onLoading.invoke()
        is StateEvent.Failure -> onFailure.invoke(state.exception)
        is StateEvent.Success -> onSuccess.invoke(state.data)
        is StateEvent.Empty -> onEmpty.invoke()
    }
}