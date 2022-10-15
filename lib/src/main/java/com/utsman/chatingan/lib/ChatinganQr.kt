package com.utsman.chatingan.lib

import android.graphics.Bitmap
import com.utsman.chatingan.lib.data.model.Contact
import com.utsman.chatingan.lib.data.pair.ContactPairListener

interface ChatinganQr {
    fun generateQrContact(): Bitmap
    suspend fun requestPair(contact: Contact): Boolean
    fun setPairListener(pairListener: ContactPairListener)
}