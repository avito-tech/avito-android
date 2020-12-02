package com.avito.android.elastic

interface Elastic {

    fun sendMessage(
        tag: String,
        level: String,
        message: String,
        throwable: Throwable?
    )
}
