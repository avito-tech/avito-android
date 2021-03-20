package com.avito.android.test.report.lifecycle

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.avito.android.test.report.Report

class ReportFragmentLifecycleListener(
    private val report: Report
) : FragmentManager.FragmentLifecycleCallbacks() {

    override fun onFragmentAttached(fm: FragmentManager, f: Fragment, context: Context) {
        report.addComment("Fragment ${f::class.java.simpleName} was ATTACHED to ${context::class.java.simpleName}")
    }

    override fun onFragmentCreated(fm: FragmentManager, f: Fragment, savedInstanceState: Bundle?) {
        report.addComment("Fragment ${f::class.java.simpleName} was CREATED")
    }

    override fun onFragmentViewCreated(fm: FragmentManager, f: Fragment, v: View, savedInstanceState: Bundle?) {
        report.addComment("Fragment ${f::class.java.simpleName} VIEW was CREATED")
    }

    override fun onFragmentStarted(fm: FragmentManager, f: Fragment) {
        report.addComment("Fragment ${f::class.java.simpleName} was STARTED")
    }

    override fun onFragmentResumed(fm: FragmentManager, f: Fragment) {
        report.addComment("Fragment ${f::class.java.simpleName} was RESUMED")
    }

    override fun onFragmentPaused(fm: FragmentManager, f: Fragment) {
        report.addComment("Fragment ${f::class.java.simpleName} was PAUSED")
    }

    override fun onFragmentStopped(fm: FragmentManager, f: Fragment) {
        report.addComment("Fragment ${f::class.java.simpleName} was STOPPED")
    }

    override fun onFragmentViewDestroyed(fm: FragmentManager, f: Fragment) {
        report.addComment("Fragment ${f::class.java.simpleName} VIEW was DESTROYED")
    }

    override fun onFragmentDestroyed(fm: FragmentManager, f: Fragment) {
        report.addComment("Fragment ${f::class.java.simpleName} was DESTROYED")
    }

    override fun onFragmentDetached(fm: FragmentManager, f: Fragment) {
        report.addComment("Fragment ${f::class.java.simpleName} was DETACHED")
    }
}
