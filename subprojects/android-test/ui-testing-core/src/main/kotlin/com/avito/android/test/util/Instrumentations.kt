package com.avito.android.test.util

import android.app.Activity
import android.os.Looper
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage

fun getCurrentActivity(): Activity {
    return getCurrentActivitySafe() ?: throw IllegalStateException("Resumed activity not found")
}

fun getCurrentActivitySafe(): Activity? {
    var currentActivity: Activity? = null
    val findResumedActivity = {
        val resumedActivities =
            ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED)
        if (resumedActivities.iterator().hasNext()) {
            currentActivity = resumedActivities.iterator().next()
        }
    }
    if (isMainThread()) {
        findResumedActivity()
    } else {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(findResumedActivity)
    }
    return currentActivity
}

internal fun isMainThread() = Looper.myLooper() == Looper.getMainLooper()
