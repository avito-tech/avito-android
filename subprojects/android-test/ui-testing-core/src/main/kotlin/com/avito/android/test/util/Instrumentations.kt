package com.avito.android.test.util

import android.app.Activity
import com.avito.android.instrumentation.ActivityProviderFactory

private val activityProvider by lazy { ActivityProviderFactory.create() }

// used in avito
fun getCurrentActivity(): Activity {
    return activityProvider.getCurrentActivity().getOrThrow()
}
