package com.utsman.chatingan.sdk.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import java.io.ByteArrayOutputStream
import java.io.File


object ImageUtils {

    fun bitmapByteArrayToString(byteArray: ByteArray): String {
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    fun bitmapByteArrayFromString(encodedString: String): ByteArray? {
        return try {
            Base64.decode(encodedString, Base64.DEFAULT)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun bitmapToBytes(bitmap: Bitmap): ByteArray {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
        return baos.toByteArray()
    }

    fun stringToBitmap(encodedString: String?): Bitmap? {
        return try {
            val encodeByte =
                Base64.decode(encodedString, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.size)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun blurImage(file: File): String {
        val bitmap = BitmapFactory.decodeFile(file.absolutePath)
        val blurBitmap = FastBlur.init(bitmap, 0.4f, 5)
        val bytesBitmap = bitmapToBytes(blurBitmap)
        return bitmapByteArrayToString(bytesBitmap)
    }
}