package com.avito.android.api

import android.util.Log

@ApiDsl
abstract class RequestRegistry {

    val registeredMocks = mutableMapOf<String, ApiRequest>()

    fun reset() {
        resetApi()
        registeredMocks.clear()
    }

    protected abstract fun resetApi()

    fun <T : ApiRequest> T.register(): T {
        Log.d("TestRunner", "${javaClass.simpleName} registered")
        registeredMocks[javaClass.simpleName] = this
        return this
    }
}
