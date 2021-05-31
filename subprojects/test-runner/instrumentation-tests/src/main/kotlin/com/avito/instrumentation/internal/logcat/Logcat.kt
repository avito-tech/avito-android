package com.avito.instrumentation.internal.logcat

internal data class Logcat(val output: String) {

    companion object {

        val STUB = Logcat("")
    }
}
