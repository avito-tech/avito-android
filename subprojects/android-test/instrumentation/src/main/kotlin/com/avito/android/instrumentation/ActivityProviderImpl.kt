package com.avito.android.instrumentation

import android.app.Activity
import android.app.Instrumentation
import android.content.ActivityNotFoundException
import android.os.Looper
import androidx.test.runner.lifecycle.ActivityLifecycleMonitor
import androidx.test.runner.lifecycle.Stage
import com.avito.android.Result

internal class ActivityProviderImpl(
    private val instrumentation: Instrumentation,
    private val activityLifecycleMonitor: ActivityLifecycleMonitor
) : ActivityProvider {

    override fun getCurrentActivity(): Result<Activity> {
        var result: Result<Activity> = notFoundResult()

        if (isMainThread()) {
            result = findResumedActivity()
        } else {
            instrumentation.runOnMainSync {
                result = findResumedActivity()
            }
        }

        return result
    }

    private fun findResumedActivity(): Result<Activity> {
        val resumedActivities = activityLifecycleMonitor.getActivitiesInStage(Stage.RESUMED)
        if (resumedActivities.iterator().hasNext()) {
            return Result.Success(resumedActivities.iterator().next())
        }
        return notFoundResult()
    }

    private fun isMainThread(): Boolean {
        val currentThreadLooper: Looper? = Looper.myLooper()
        return currentThreadLooper == Looper.getMainLooper()
    }

    private fun notFoundResult(): Result<Activity> {
        return Result.Failure(ActivityNotFoundException("No activities in RESUMED state found"))
    }
}
