package com.utsman.chatingan.lib.utils

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.utsman.chatingan.lib.data.model.Contact

object ChatinganQrUtils {

    private const val QR_PIXEL_SIZE = 600

    internal fun generateQrBitmap(content: String): Bitmap {
        val bits = QRCodeWriter()
            .encode(content, BarcodeFormat.QR_CODE, QR_PIXEL_SIZE, QR_PIXEL_SIZE)

        val resultBitmap = Bitmap
            .createBitmap(QR_PIXEL_SIZE, QR_PIXEL_SIZE, Bitmap.Config.RGB_565).apply {
                for (x in 0 until QR_PIXEL_SIZE) {
                    for (y in 0 until QR_PIXEL_SIZE) {
                        val color = if (bits[x, y]) {
                            Color.BLACK
                        } else {
                            Color.WHITE
                        }
                        setPixel(x, y, color)
                    }
                }
            }
        return resultBitmap
    }

    fun generateContactFromPair(content: String): Contact {
        return Utils.convertFromJson(content)
    }
}