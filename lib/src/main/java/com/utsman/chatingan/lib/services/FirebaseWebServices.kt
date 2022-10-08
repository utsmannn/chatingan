package com.utsman.chatingan.lib.services

import com.utsman.chatingan.lib.ChatinganConfiguration
import com.utsman.chatingan.lib.data.firebase.FirebaseMessageRequest
import com.utsman.chatingan.lib.data.firebase.FirebaseMessagingResponse
import okhttp3.Headers.Companion.toHeaders
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

internal interface FirebaseWebServices {

    @POST(ENDPOINT.SEND)
    suspend fun sendMessage(
        @Body messageBody: FirebaseMessageRequest
    ): Response<FirebaseMessagingResponse>

    object ENDPOINT {
        const val SEND = "fcm/send"
    }

    companion object {
        private const val BASE_URL = "https://fcm.googleapis.com/"

        fun getInstance(config: ChatinganConfiguration): FirebaseWebServices {
            val authData = mapOf(
                "Authorization" to "key=${config.fcmServerKey}"
            )

            val loggingInterceptor = HttpLoggingInterceptor()
            loggingInterceptor.level = HttpLoggingInterceptor.Level.BASIC

            val authInterceptor = Interceptor { chain ->
                val process = chain.run {
                    val request = request()
                        .newBuilder()
                        .headers(authData.toHeaders())
                        .build()
                    proceed(request)
                }
                return@Interceptor process
            }

            val okHttp = OkHttpClient()
                .newBuilder()
                .addInterceptor(loggingInterceptor)
                .addInterceptor(authInterceptor)
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttp)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(FirebaseWebServices::class.java)
        }
    }
}