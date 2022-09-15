package com.utsman.chatingan

import android.app.Application
import com.utsman.chatingan.auth.AuthModule
import com.utsman.chatingan.auth.data.AuthConfig
import com.utsman.chatingan.common.IOScope
import com.utsman.chatingan.common.koin.moduleListOf
import com.utsman.chatingan.home.HomeModule
import com.utsman.chatingan.sdk.ChatinganFirebase
import com.utsman.chatingan.sdk.ChatinganSdkModules
import kotlinx.coroutines.launch
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class ChatinganApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        ChatinganFirebase.init()
        startKoin {
            androidContext(this@ChatinganApplication)
            moduleListOf(
                ChatinganSdkModules.module(),
                ChatinganModule.modules(),
                AuthModule.modules(),
                HomeModule.modules()
            )
        }

        IOScope().launch {
            val authConfig: AuthConfig = get()
            authConfig.clientId = getString(R.string.default_web_client_id)

            val token = ChatinganFirebase.fcmToken()
            println(token)
        }
    }
}