package com.avito.android.screen

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
     *
     * Also used to link "Screen" PageObject and "Screen" (android.view.View) in app code
     * for impact analysis in ui tests
     *
     * <WARNING>
     * Use only direct implementation(ex: rootId = R.id.root)!
     * Not lateinit or custom getters to init property
     * Static code analyzer for impact analyze works on bytecode level and couldn't extract value otherwise
     *
     * if apps's screen is in separate module, there can be multiple equal id's, like:
     *  com.avito.android.authorization.R.id
     *  com.avito.android.R.id
     * Even worse, if there is a name clash (ex: id.root in multiple modules) LAST ONE! ends up in com.avito.android.R.id
     * Last one means last module in resource merge process
     * We could get different isOpened results based on module merge ordering :crazy:
     * So you better import fully qualified package name for R class of your module in Screen implementation
     * </WARNING>
     */
    val rootId: Int

    val checks: ScreenChecks
        get() = StrictScreenChecks(screen = this, checkOnEachScreenInteraction = false)

    companion object {

        /**
         * means that all tests that calls this screen will be run regardless of code changes (no impact analysis)
         */
        const val UNKNOWN_ROOT_ID: Int = -1
    }
}
