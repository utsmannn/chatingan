package com.utsman.chatingan.network

import okhttp3.Headers.Companion.toHeaders
import okhttp3.Interceptor

object InterceptorBuilder {

    fun create(keyValue: Map<String, String> = emptyMap()): Interceptor =
        Interceptor { chain ->
            val process = chain.run {
                val request = request()
                    .newBuilder()
                    .headers(keyValue.toHeaders())
                    .build()
                proceed(request)
            }
            return@Interceptor process
        }
}