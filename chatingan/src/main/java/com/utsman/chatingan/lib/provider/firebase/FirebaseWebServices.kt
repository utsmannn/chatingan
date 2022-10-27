package com.utsman.chatingan.lib.provider.firebase

import com.utsman.chatingan.lib.data.network.firebase.FirebaseMessageRequest
import com.utsman.chatingan.lib.data.network.firebase.FirebaseMessagingResponse
import okhttp3.Headers.Companion.toHeaders
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import retrofit2.http.Body
import retrofit2.http.POST

internal interface FirebaseWebServices {

    @POST(Endpoint.SEND)
    suspend fun sendMessage(
        @Body messageBody: FirebaseMessageRequest
    ): Response<FirebaseMessagingResponse>

    object Endpoint {
        const val SEND = "fcm/send"
    }

    companion object {
        private const val BASE_URL = "https://fcm.googleapis.com/"

        fun getInstance(
            fcmServerKey: String,
            logLevel: FirebaseEmitter.LogLevel
        ): FirebaseWebServices {
            val authData = mapOf(
                "Authorization" to "key=$fcmServerKey"
            )

            val loggingInterceptor = HttpLoggingInterceptor()
            loggingInterceptor.level = when (logLevel) {
                FirebaseEmitter.LogLevel.NONE -> HttpLoggingInterceptor.Level.BODY
                FirebaseEmitter.LogLevel.BASIC -> HttpLoggingInterceptor.Level.BASIC
                FirebaseEmitter.LogLevel.BODY -> HttpLoggingInterceptor.Level.BODY
                FirebaseEmitter.LogLevel.HEADER -> HttpLoggingInterceptor.Level.HEADERS
            }

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

            val okHttp = OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .addInterceptor(authInterceptor)
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttp)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create()
        }
    }
}