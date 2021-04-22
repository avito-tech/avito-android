package com.avito.android.test.util

import android.app.Activity
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import com.avito.android.instrumentation.ActivityProviderFactory

private val activityProvider = ActivityProviderFactory.create(
    instrumentation = InstrumentationRegistry.getInstrumentation(),
    activityLifecycleMonitor = ActivityLifecycleMonitorRegistry.getInstance()
)

// used in avito
fun getCurrentActivity(): Activity {
    return activityProvider.getCurrentActivity().getOrThrow()
}
