package com.avito.android.test.report.lifecycle

import android.app.Activity
import androidx.fragment.app.FragmentActivity
import androidx.test.runner.lifecycle.ActivityLifecycleCallback
import androidx.test.runner.lifecycle.Stage

class ReportActivityLifecycleListener(
    factory: com.avito.logger.LoggerFactory
) : ActivityLifecycleCallback {

    private val logger = factory.create("ReportActivityLifecycle")
    private val fragmentLifecycleListener = ReportFragmentLifecycleListener(factory)

    override fun onActivityLifecycleChanged(activity: Activity, stage: Stage) {
        val message = "Activity ${activity::class.java.simpleName} was $stage"
        logger.info(message)
        when (stage) {
            Stage.PRE_ON_CREATE -> if (activity is FragmentActivity) {
                /**
                 * Look to [androidx.fragment.app.FragmentManager.unregisterFragmentLifecycleCallbacks]
                 * All registered callbacks will be
                 * automatically unregistered when this FragmentManager is destroyed
                 */
                activity
                    .supportFragmentManager
                    .registerFragmentLifecycleCallbacks(
                        fragmentLifecycleListener,
                        true
                    )
            }
            Stage.CREATED,
            Stage.RESUMED,
            Stage.PAUSED,
            Stage.DESTROYED -> {
                // todo Enable after MBS-11010 report.addComment(message)
            }
            else -> {
                // do nothing to avoid a lot of events
            }
        }
    }
}
