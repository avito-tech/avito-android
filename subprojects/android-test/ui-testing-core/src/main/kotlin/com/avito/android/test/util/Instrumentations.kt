package com.avito.android.test.util

import android.app.Activity
import com.avito.android.instrumentation.ActivityProviderFactory

private val activityProvider = ActivityProviderFactory.create()

fun getCurrentActivity(): Activity {
    return getCurrentActivityOrNull() ?: throw IllegalStateException("Resumed activity not found")
}

fun getCurrentActivityOrNull(): Activity? {
    return activityProvider.getCurrentActivity()
}
