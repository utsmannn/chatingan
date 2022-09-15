package com.utsman.chatingan.sdk

import com.utsman.chatingan.sdk.services.FirebaseServices

object ChatinganSdkModules {

    fun module() = listOf(
        FirebaseServices.inject()
    )
}