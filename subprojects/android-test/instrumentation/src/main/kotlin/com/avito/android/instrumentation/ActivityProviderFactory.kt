package com.avito.android.instrumentation

import android.app.Instrumentation
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.lifecycle.ActivityLifecycleMonitor
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry

object ActivityProviderFactory {

    fun create(
        instrumentation: Lazy<Instrumentation> = lazy {
            InstrumentationRegistry.getInstrumentation()
        },
        activityLifecycleMonitor: Lazy<ActivityLifecycleMonitor> = lazy {
            ActivityLifecycleMonitorRegistry.getInstance()
        }
    ): ActivityProvider {
        return ActivityProviderImpl(instrumentation, activityLifecycleMonitor)
    }
}
