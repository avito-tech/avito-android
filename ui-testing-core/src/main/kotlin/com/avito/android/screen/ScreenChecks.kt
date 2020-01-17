package com.avito.android.screen

import android.annotation.SuppressLint
import androidx.annotation.CallSuper
import androidx.test.espresso.matcher.ViewMatchers
import com.avito.android.screen.Screen.Companion.UNKNOWN_ROOT_ID
import com.avito.android.test.page_object.ViewElement

interface ScreenChecks {

    val screen: Screen

    // TODO: MBS-7204 Migrate all screens to stricter isDisplayed
    //  and merge this method with isScreenOpened after that
    @Deprecated("Use isScreenOpened() instead and ",
        replaceWith = ReplaceWith("isScreenOpened()")
    )
    @CallSuper
    fun isOpened() {
        screen.checkRootId()
    }

    /**
     * Check whether screen is visible for user.
     */
    @CallSuper
    fun isScreenOpened() {
    }
}

// TODO: MBS-7204 Migrate all screens to StrictScreenChecks and delete this class
@Deprecated("Use StrictScreenChecks instead",
    replaceWith = ReplaceWith("StrictScreenChecks")
)
class SimpleScreenChecks(override val screen: Screen) : ScreenChecks

open class StrictScreenChecks(override val screen: Screen) : ScreenChecks {

    @SuppressLint("MissingSuperCall")
    override fun isOpened() = isScreenOpened() // for invokes from tests

    override fun isScreenOpened() {
        super.isScreenOpened()
        screen.checkRootId()
    }
}

private fun Screen.checkRootId() {
    if (this.rootId != UNKNOWN_ROOT_ID) {
        val rootElement = ViewElement(ViewMatchers.withId(this.rootId))
        rootElement.checks.exists()
        rootElement.checks.isDisplayed()
    }
}
