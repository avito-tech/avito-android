package com.avito.android.test.report.lifecycle

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.avito.logger.LoggerFactory

class ReportFragmentLifecycleListener(
    factory: LoggerFactory,
) : FragmentManager.FragmentLifecycleCallbacks() {

    private val logger = factory.create("ReportFragmentLifecycle")

    override fun onFragmentAttached(fm: FragmentManager, f: Fragment, context: Context) {
        val message = "Fragment ${f::class.java.simpleName} was ATTACHED to ${context::class.java.simpleName}"
        logger.info(message)
        // todo Enable after MBS-11010 report.addComment(message)
    }

    override fun onFragmentCreated(fm: FragmentManager, f: Fragment, savedInstanceState: Bundle?) {
        logger.info("Fragment ${f::class.java.simpleName} was CREATED")
    }

    override fun onFragmentViewCreated(fm: FragmentManager, f: Fragment, v: View, savedInstanceState: Bundle?) {
        logger.info("Fragment ${f::class.java.simpleName} VIEW was CREATED")
    }

    override fun onFragmentStarted(fm: FragmentManager, f: Fragment) {
        logger.info("Fragment ${f::class.java.simpleName} was STARTED")
    }

    override fun onFragmentResumed(fm: FragmentManager, f: Fragment) {
        val message = "Fragment ${f::class.java.simpleName} was RESUMED"
        logger.info(message)
        // todo Enable after MBS-11010 report.addComment(message)
    }

    override fun onFragmentPaused(fm: FragmentManager, f: Fragment) {
        val message = "Fragment ${f::class.java.simpleName} was PAUSED"
        logger.info(message)
        // todo Enable after MBS-11010 report.addComment(message)
    }

    override fun onFragmentStopped(fm: FragmentManager, f: Fragment) {
        logger.info("Fragment ${f::class.java.simpleName} was STOPPED")
    }

    override fun onFragmentViewDestroyed(fm: FragmentManager, f: Fragment) {
        logger.info("Fragment ${f::class.java.simpleName} VIEW was DESTROYED")
    }

    override fun onFragmentDestroyed(fm: FragmentManager, f: Fragment) {
        val message = "Fragment ${f::class.java.simpleName} was DESTROYED"
        logger.info(message)
        // todo Enable after MBS-11010 report.addComment(message)
    }

    override fun onFragmentDetached(fm: FragmentManager, f: Fragment) {
        val message = "Fragment ${f::class.java.simpleName} was DETACHED"
        logger.info(message)
        // todo Enable after MBS-11010 report.addComment(message)
    }
}
