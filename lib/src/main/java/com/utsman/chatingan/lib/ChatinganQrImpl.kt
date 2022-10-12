package com.utsman.chatingan.lib

import android.graphics.Bitmap
import com.utsman.chatingan.lib.data.model.Contact
import com.utsman.chatingan.lib.data.pair.ContactPairListener

class ChatinganQrImpl(
    private val contact: Contact,
    private val onRequest: suspend (Contact, ChatinganQrImpl) -> Boolean
) : ChatinganQr {

    private var pairListener: ContactPairListener? = null

    override fun generateQrContact(): Bitmap {
        val contentJson = contact.toJson()
        return ChatinganQrUtils.generateQrBitmap(content = contentJson)
    }

    override suspend fun requestPair(contact: Contact): Boolean {
        return onRequest.invoke(contact, this)
    }

    override fun setPairListener(pairListener: ContactPairListener) {
        this.pairListener = pairListener
    }

    internal fun setPairListenerSuccess(contact: Contact) {
        pairListener?.onPairSuccess(contact)
    }

    internal fun setPairListenerFailure(throwable: Throwable) {
        pairListener?.onPairFailed(throwable)
    }
}