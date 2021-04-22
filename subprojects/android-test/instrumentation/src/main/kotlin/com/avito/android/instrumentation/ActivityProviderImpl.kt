package com.avito.android.instrumentation

import android.app.Activity
import android.app.Instrumentation
import android.content.ActivityNotFoundException
import android.os.Looper
import androidx.test.runner.lifecycle.ActivityLifecycleMonitor
import androidx.test.runner.lifecycle.Stage
import com.avito.android.Result

internal class ActivityProviderImpl(
    private val instrumentation: Lazy<Instrumentation>,
    private val activityLifecycleMonitor: Lazy<ActivityLifecycleMonitor>
) : ActivityProvider {

    override fun getCurrentActivity(): Result<Activity> {
        var result: Result<Activity> = notFoundResult()

        if (isMainThread()) {
            result = findResumedActivity()
        } else {
            try {
                instrumentation.value.runOnMainSync {
                    result = findResumedActivity()
                }
            } catch (e: Throwable) {
                result = Result.Failure(e)
            }
        }

        return result
    }

    private fun findResumedActivity(): Result<Activity> {
        try {
            val resumedActivities = activityLifecycleMonitor.value.getActivitiesInStage(Stage.RESUMED)
            if (resumedActivities.iterator().hasNext()) {
                return Result.Success(resumedActivities.iterator().next())
            }
        } catch (e: Throwable) {
            return Result.Failure(e)
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
