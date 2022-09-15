package com.utsman.chatingan.sdk.services

import com.utsman.chatingan.common.koin.KoinInjector
import com.utsman.chatingan.network.InterceptorBuilder
import com.utsman.chatingan.network.RetrofitBuilder
import com.utsman.chatingan.sdk.data.FirebaseMessageRequest
import org.koin.core.module.Module
import org.koin.dsl.module
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface FirebaseServices {

    @POST(ENDPOINT.SEND)
    suspend fun sendMessage(
        @Body messageBody: FirebaseMessageRequest
    ): Response<Any>

    object ENDPOINT {
        const val SEND = "fcm/send"
    }

    companion object : KoinInjector {
        private const val BASE_URL = "https://fcm.googleapis.com/"
        private const val SERVER_KEY = "AAAAuXTwdzI:APA91bFQm5BAlwwF8XEuEmw4s3hMzH8b4vQGy9sK2rsahAQh0rxQRZvZQxBbYzzx7VKHg98J2t6O2dofvJy3qxWLU7AhNvpB1JqlmzdcF1ql52VRLnpjxEWP6B8o2kQMV4Ms6UZN4AxH"

        private fun build(): FirebaseServices {
            val authData = mapOf(
                "Authorization" to "key=$SERVER_KEY"
            )
            val headerInterceptor = InterceptorBuilder.create(authData)
            return RetrofitBuilder.build(url = BASE_URL, interceptor = headerInterceptor)
        }

        override fun inject(): Module {
            return module {
                single { build() }
            }
        }
    }
}