package com.avito.android.instrumentation

object ActivityProviderFactory {

    fun create(): ActivityProvider {
        return ActivityProviderImpl()
    }
}
