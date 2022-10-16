package com.utsman.chatingan.lib.provider

import com.utsman.chatingan.lib.data.network.image.ImageData
import java.io.File

abstract class ImageUploader {
    abstract suspend fun upload(file: File): Result<ImageData>
}