package com.utsman.chatingan.sdk.utils

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter

object ChatinganUtils {
    private val typingFlow = MutableStateFlow(false)

    suspend fun textIsTyping(text: Flow<String>): Flow<Boolean> {
        text.debounce(100)
            .filter { it.length > 2 }
            .collect {
                typingFlow.value = true

                delay(1000)
                typingFlow.value = false
            }
        return typingFlow
    }
}