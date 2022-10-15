package com.utsman.chatingan.lib.services

import com.utsman.chatingan.lib.data.network.image.FreeImageHostResponse
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface FreeImageHostWebServices {

    @Multipart
    @POST(Endpoint.UPLOAD)
    suspend fun uploadImage(
        @Query(QueryKey.KEY) key: String,
        @Part imagePart: MultipartBody.Part
    ): Response<FreeImageHostResponse>

    object Endpoint {
        const val UPLOAD = "api/1/upload"
    }

    object QueryKey {
        const val KEY = "key"
    }

    companion object {
        private const val BASE_URL = "https://freeimage.host/"

        fun getInstance(): FreeImageHostWebServices {
            val loggingInterceptor = HttpLoggingInterceptor()
            loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

            val okHttp = OkHttpClient()
                .newBuilder()
                .addInterceptor(loggingInterceptor)
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