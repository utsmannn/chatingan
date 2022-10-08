package com.utsman.chatingan.lib

import android.graphics.Bitmap
import com.utsman.chatingan.lib.data.model.Contact
import com.utsman.chatingan.lib.data.pair.ContactPairListener

class ChatinganQrImpl(
    private val configuration: ChatinganConfiguration,
    private val onRequest: suspend (Contact, ChatinganQrImpl) -> Boolean
) : ChatinganQr {

    //private var pairListeners: MutableList<ContactPairListener> = mutableListOf()
    private var pairListener: ContactPairListener? = null

    override fun generateQrContact(): Bitmap {
        val contact = configuration.contact
        val pairData = DataMapper.mapContactToPairData(contact)
        val contentJson = pairData.toJson()
        return ChatinganQrUtils.generateQrBitmap(content = contentJson)
    }

    override suspend fun requestPair(contact: Contact): Boolean {
        return onRequest.invoke(contact, this)
    }

    override fun setPairListener(pairListener: ContactPairListener) {
        //pairListeners.add(pairListener)
        this.pairListener = pairListener
    }

    internal fun setPairListenerSuccess(contact: Contact) {
        //pairListeners.forEach { it.onPairSuccess(contact) }
        pairListener?.onPairSuccess(contact)
    }

    internal fun setPairListenerFailure(throwable: Throwable) {
        pairListener?.onPairFailed(throwable)
    }
}