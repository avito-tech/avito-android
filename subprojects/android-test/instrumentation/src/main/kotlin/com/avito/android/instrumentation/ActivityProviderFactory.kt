package com.avito.android.instrumentation

import android.app.Instrumentation
import androidx.test.runner.lifecycle.ActivityLifecycleMonitor

object ActivityProviderFactory {

    fun create(
        instrumentation: Instrumentation,
        activityLifecycleMonitor: ActivityLifecycleMonitor
    ): ActivityProvider {
        return ActivityProviderImpl(instrumentation, activityLifecycleMonitor)
    }
}
