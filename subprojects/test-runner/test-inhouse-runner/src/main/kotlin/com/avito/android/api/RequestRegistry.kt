package com.avito.android.api

import android.util.Log
@ApiDsl
abstract class RequestRegistry {

    val registeredMocks = mutableMapOf<String, ApiRequest>()
    val registeredMocksCrt = mutableMapOf<String, ApiRequestCrt>()

    fun reset() {
        resetApi()
        registeredMocks.clear()
        registeredMocksCrt.clear()
    }

    protected abstract fun resetApi()

    fun <T : ApiRequest> T.register(): T {
        Log.d("TestRunner", "${javaClass.simpleName} registered")
        registeredMocks[javaClass.simpleName] = this
        return this
    }

    fun <T : ApiRequestCrt> T.registerCrt(): T {
        Log.d("TestRunner", "${javaClass.simpleName} registered")
        registeredMocksCrt[javaClass.simpleName] = this
        return this
    }
}
