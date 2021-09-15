package com.avito.android.screen

import android.view.View

/**
 * Single "screen" of an app
 * Could be activity or fragment or view based
 *
 * Also used for navigation drawer and dialogs for now
 * todo consider separate interface?
 */
interface Screen {

    /**
     * R.id of root view in hierarchy of a screen
     * Used to determine if screen is presented to user (opened)
     */
    val rootId: Int

    // TODO: remove default implementation after migrating clients to specific implementations in MBS-11808
    @Suppress("DEPRECATION")
    val checks: ScreenChecks
        get() = StrictScreenChecks(screen = this, checkOnEachScreenInteraction = false)

    companion object {

        /**
         * means that all tests that calls this screen will be run regardless of code changes (no impact analysis)
         */
        const val UNKNOWN_ROOT_ID: Int = View.NO_ID
    }
}
