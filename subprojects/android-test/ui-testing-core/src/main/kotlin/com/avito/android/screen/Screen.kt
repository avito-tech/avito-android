package com.avito.android.screen

/**
 * Single "screen" of an app
 * Could be activity or fragment or view based
 *
 * Also used for navigation drawer and dialogs for now
 */
interface Screen {

    /**
     * R.id of root view in hierarchy of a screen
     * Used to determine if screen is presented to user (opened)
     */
    val rootId: Int

    /**
     * Should be a full path to a module containing it's [rootId]
     *
     * Used to link "Screen" PageObject and "Screen" (android.view.View) in app code
     * for impact analysis in ui tests
     */
    val modulePath: String

    val checks: ScreenChecks
        get() = SimpleScreenChecks(this) // TODO: replace by StrictScreenChecks MBS-7204

    companion object {

        /**
         * means that all tests that calls this screen will be run regardless of code changes (no impact analysis)
         */
        const val UNKNOWN_ROOT_ID: Int = -1
    }
}
