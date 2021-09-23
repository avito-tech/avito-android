package com.avito.android.screen

import android.view.View

/**
 * Single "screen" of an app
 * Could be activity, fragment, dialog or view
 */
interface Screen {

    /**
     * R.id of root view in hierarchy of a screen
     * Used to determine if screen is presented to user (opened)
     */
    val rootId: Int

    val checks: ScreenChecks

    companion object {

        /**
         * means that all tests that calls this screen will be run regardless of code changes (no impact analysis)
         */
        const val UNKNOWN_ROOT_ID: Int = View.NO_ID
    }
}
