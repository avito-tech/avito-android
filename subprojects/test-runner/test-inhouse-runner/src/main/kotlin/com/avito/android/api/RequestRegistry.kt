package com.avito.android.api

import android.util.Log

@ApiDsl
public abstract class RequestRegistry {

    internal val registeredMocks = mutableMapOf<String, ApiRequest>()

    public fun reset() {
        resetApi()
        registeredMocks.clear()
    }

    protected abstract fun resetApi()

    public fun <T : ApiRequest> T.register(): T {
        Log.d("TestRunner", "${javaClass.simpleName} registered")
        registeredMocks[javaClass.simpleName] = this
        return this
    }
}
