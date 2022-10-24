package com.avito.android.api

import android.util.Log
@ApiDsl
abstract class RequestRegistry {

    val registeredMocks = mutableMapOf<String, ApiRequest>()
    val registeredSuspendMocks = mutableMapOf<String, ApiRequestSuspend>()

    fun reset() {
        resetApi()
        registeredMocks.clear()
        registeredSuspendMocks.clear()
    }

    protected abstract fun resetApi()

    fun <T : ApiRequest> T.register(): T {
        Log.d("TestRunner", "${javaClass.simpleName} registered")
        registeredMocks[javaClass.simpleName] = this
        return this
    }

    fun <T : ApiRequestSuspend> T.registerSuspend(): T {
        Log.d("TestRunner", "${javaClass.simpleName} registered")
        registeredSuspendMocks[javaClass.simpleName] = this
        return this
    }
}
