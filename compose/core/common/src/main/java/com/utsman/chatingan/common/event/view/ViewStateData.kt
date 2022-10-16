package com.utsman.chatingan.common.event.view

fun interface ViewStateData<T> {
    fun run(data: T)
}