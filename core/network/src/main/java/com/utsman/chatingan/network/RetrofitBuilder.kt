package com.utsman.chatingan.network

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitBuilder {
    inline fun <reified T>build(
        url: String,
        interceptor: Interceptor? = null
    ): T {
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BASIC

        val okHttp =  OkHttpClient()
            .newBuilder()
            .addInterceptor(loggingInterceptor)
            .apply {
                if (interceptor != null) {
                    addInterceptor(interceptor)
                }
            }
            .build()
        return Retrofit.Builder()
            .baseUrl(url)
            .client(okHttp)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(T::class.java)
    }
}