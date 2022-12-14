package com.utsman.chatingan.lib.provider.freeimagehost

import com.utsman.chatingan.lib.data.network.image.ImageData
import com.utsman.chatingan.lib.provider.ImageUploader
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class FreeImageUploader(private val key: String) : ImageUploader() {
    private val freeImageHostWebServices: FreeImageHostWebServices
        get() = FreeImageHostWebServices.getInstance()

    private fun File.createPartBody(): MultipartBody.Part {
        return MultipartBody.Part.createFormData(
            name = "source",
            filename = name,
            body = asRequestBody()
        )
    }

    override suspend fun upload(file: File): Result<ImageData> {
        val uploadResponse = freeImageHostWebServices.uploadImage(
            key = key,
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