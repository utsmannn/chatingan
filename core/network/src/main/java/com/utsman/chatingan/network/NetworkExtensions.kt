package com.utsman.chatingan.network

import com.utsman.chatingan.common.IOScope
import com.utsman.chatingan.common.event.FlowEvent
import com.utsman.chatingan.common.event.StateEvent
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import retrofit2.Response

suspend fun <T> Response<T>.asFlowEvent(): FlowEvent<T> {
    return flow {
        emit(StateEvent.Loading())
        delay(2000)
        val emitData = try {
            val body = body()
            if (isSuccessful && body != null) {
                StateEvent.Success<T>(body)
            } else {
                val message = errorBody()?.string().orEmpty()
                val exception = Throwable(message = message)
                StateEvent.Failure(exception)
            }
        } catch (e: Throwable) {
            StateEvent.Failure(e)
        }

        emit(emitData)
    }.stateIn(IOScope())
}