package com.avito.android.test.util

import androidx.test.platform.app.InstrumentationRegistry
import me.weishu.reflection.Reflection

/**
 * Workaround to access restricted API by reflection.
 * https://developer.android.com/distribute/best-practices/develop/restrictions-non-sdk-interfaces
 */
object HiddenApiOpener {

    private val unsealResult: Boolean by lazy {
        0 == Reflection.unseal(InstrumentationRegistry.getInstrumentation().targetContext)
    }

    fun ensureUnseal() = check(unsealResult) {
        "Hidden API is unavailable"
    }
}
