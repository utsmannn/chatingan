package com.utsman.chatingan

import android.app.Application
import com.utsman.chatingan.auth.AuthModule
import com.utsman.chatingan.auth.data.AuthConfig
import com.utsman.chatingan.auth.datasources.AuthDataSources
import com.utsman.chatingan.chat.di.ChatModule
import com.utsman.chatingan.common.IOScope
import com.utsman.chatingan.common.koin.moduleListOf
import com.utsman.chatingan.contact.di.ContactModule
import com.utsman.chatingan.di.AppModule
import com.utsman.chatingan.home.HomeModule
import com.utsman.chatingan.lib.Chatingan
import kotlinx.coroutines.launch
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class AppApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Chatingan.initialize(this) {
            fcmServerKey = SERVER_KEY
            freeImageHostApiKey = IMGHOST_API_KEY
        }

        startKoin {
            androidContext(this@AppApplication)
            moduleListOf(
                AuthModule.modules(),
                AppModule.modules(),
                ContactModule.modules(),
                HomeModule.modules(),
                ChatModule.modules()
            )
        }

        IOScope().launch {
            val authConfig: AuthConfig = get()
            authConfig.clientId = getString(R.string.default_web_client_id)

            val authDataSources: AuthDataSources = get()
            val token = authDataSources.firebaseToken()
            println("--- YOUR TOKEN ---")
            println(token)
            println("--- END YOUR TOKEN ---")

            println("--- TRY SET CHATINGAN INSTANCE ---")
        }
    }

    companion object {
        private const val SERVER_KEY = "AAAAuXTwdzI:APA91bFQm5BAlwwF8XEuEmw4s3hMzH8b4vQGy9sK2rsahAQh0rxQRZvZQxBbYzzx7VKHg98J2t6O2dofvJy3qxWLU7AhNvpB1JqlmzdcF1ql52VRLnpjxEWP6B8o2kQMV4Ms6UZN4AxH"
        private const val IMGHOST_API_KEY = "6d207e02198a847aa98d0a2a901485a5"
    }
}