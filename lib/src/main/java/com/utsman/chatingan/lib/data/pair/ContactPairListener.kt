package com.utsman.chatingan.lib.data.pair

import com.utsman.chatingan.lib.data.model.Contact

interface ContactPairListener {
    fun onPairSuccess(contact: Contact)
    fun onPairFailed(throwable: Throwable)
}