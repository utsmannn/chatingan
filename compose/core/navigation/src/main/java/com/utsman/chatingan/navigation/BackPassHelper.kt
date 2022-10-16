package com.utsman.chatingan.navigation

import kotlinx.coroutines.flow.StateFlow

interface BackPassHelper {
    val currentBack: StateFlow<String>
    fun setBackFrom(screenName: String)
    fun clearBackFrom()
}