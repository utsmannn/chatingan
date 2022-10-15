package com.utsman.chatingan.sdk.storage

import com.google.firebase.storage.FirebaseStorage

class ImageStorage {

    private val storage: FirebaseStorage
        get() = FirebaseStorage.getInstance()

    fun upload(bytes: ByteArray, name: String) {
        val reference = storage.reference.child("image/$name.jpg")
        reference.activeUploadTasks.map {
        }
        reference.putBytes(bytes)
            .addOnProgressListener {
                val uploadSession = it.uploadSessionUri
                val total = it.totalByteCount
                val current = it.bytesTransferred
            }
    }

}