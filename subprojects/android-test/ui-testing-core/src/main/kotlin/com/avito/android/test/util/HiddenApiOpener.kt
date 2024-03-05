package com.avito.android.test.util

import androidx.test.platform.app.InstrumentationRegistry
import me.weishu.reflection.Reflection

/**
 * Workaround to access restricted API by reflection.
 * https://developer.android.com/distribute/best-practices/develop/restrictions-non-sdk-interfaces
 */
public object HiddenApiOpener {

    private val unsealResult: Boolean by lazy {
        0 == Reflection.unseal(InstrumentationRegistry.getInstrumentation().targetContext)
    }

    public fun ensureUnseal(): Unit = check(unsealResult) {
        "Hidden API is unavailable"
    }
}
