package com.avito.android.elastic

object StubElastic : Elastic {
    override fun sendMessage(tag: String, level: String, message: String, throwable: Throwable?) {
    }
}
