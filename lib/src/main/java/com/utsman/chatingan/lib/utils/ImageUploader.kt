package com.utsman.chatingan.lib.utils

import com.utsman.chatingan.lib.configuration.ChatinganConfiguration
import com.utsman.chatingan.lib.data.network.image.ImageData
import com.utsman.chatingan.lib.services.FreeImageHostWebServices
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class ImageUploader(private val configuration: ChatinganConfiguration) {

    private val freeImageHostWebServices: FreeImageHostWebServices
        get() = FreeImageHostWebServices.getInstance()

    private fun File.createPartBody(): MultipartBody.Part {
        return MultipartBody.Part.createFormData(
            name = "source",
            filename = name,
            body = asRequestBody()
        )
    }

    suspend fun upload(file: File): Result<ImageData> {
        val uploadResponse = freeImageHostWebServices.uploadImage(
            key = configuration.freeImageHostApiKey,
            imagePart = file.createPartBody()
        )

        val body = uploadResponse.body()
        if (!uploadResponse.isSuccessful || body == null) return Result.failure(Throwable("Failed"))

        val imageUrl = body.image?.image?.url
        val thumbUrl = body.image?.thumb?.url

        if (imageUrl != null && thumbUrl != null) {
            val imageData = ImageData(imageUrl, thumbUrl)
            return Result.success(imageData)
        }

        return Result.failure(Throwable("Failed"))
    }
}