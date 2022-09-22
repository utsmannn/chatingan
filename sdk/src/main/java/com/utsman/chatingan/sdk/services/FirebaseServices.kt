package com.utsman.chatingan.sdk.services

import com.utsman.chatingan.network.InterceptorBuilder
import com.utsman.chatingan.network.RetrofitBuilder
import com.utsman.chatingan.sdk.data.FirebaseMessageRequest
import com.utsman.chatingan.sdk.data.config.ChatinganConfig
import com.utsman.chatingan.sdk.data.response.FirebaseMessagingResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

internal interface FirebaseServices {

    @POST(ENDPOINT.SEND)
    suspend fun sendMessage(
        @Body messageBody: FirebaseMessageRequest
    ): Response<FirebaseMessagingResponse>

    object ENDPOINT {
        const val SEND = "fcm/send"
    }

    companion object {
        private const val BASE_URL = "https://fcm.googleapis.com/"

        fun instance(config: ChatinganConfig): FirebaseServices {
            val authData = mapOf(
                "Authorization" to "key=${config.serverKey}"
            )
            val headerInterceptor = InterceptorBuilder.create(authData)
            return RetrofitBuilder.build(url = BASE_URL, interceptor = headerInterceptor)
        }
    }
}