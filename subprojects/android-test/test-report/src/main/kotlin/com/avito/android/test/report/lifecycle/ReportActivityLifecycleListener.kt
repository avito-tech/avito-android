package com.avito.android.test.report.lifecycle

import android.app.Activity
import androidx.fragment.app.FragmentActivity
import androidx.test.runner.lifecycle.ActivityLifecycleCallback
import androidx.test.runner.lifecycle.Stage
import com.avito.android.test.report.Report

class ReportActivityLifecycleListener(private val report: Report) : ActivityLifecycleCallback {

    private val fragmentLifecycleListener = ReportFragmentLifecycleListener(report)

    override fun onActivityLifecycleChanged(activity: Activity, stage: Stage) {
        report.addComment("Activity ${activity::class.java.simpleName} was $stage")
        when (stage) {
            Stage.PRE_ON_CREATE ->
                if (activity is FragmentActivity) {
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
            else -> {
                // register only on PRE_CREATE
            }
        }
    }
}
