package com.avito.android.screen

import androidx.annotation.CallSuper
import androidx.test.espresso.matcher.ViewMatchers
import com.avito.android.screen.Screen.Companion.UNKNOWN_ROOT_ID
import com.avito.android.test.page_object.ViewElement

/**
 * MBS-7204 Screenchecks Story
 */
interface ScreenChecks {

    val checkOnEachScreenInteraction: Boolean
        get() = false

    fun isScreenOpened()
}

open class StrictScreenChecks<out T : Screen>(
    protected val screen: T,
    final override val checkOnEachScreenInteraction: Boolean = true
) : ScreenChecks {

    @CallSuper
    override fun isScreenOpened() {
        screen.checkRootId()
    }
}

/**
 * Known limitations: don't work in Android 11 in case of multiple windows (dialogs, popups, ...)
 * Will be fixed in MBS-11808
 */
private fun Screen.checkRootId() {
    if (this.rootId != UNKNOWN_ROOT_ID) {
        val rootElement = ViewElement(ViewMatchers.withId(this.rootId))
        rootElement.checks.exists()
        rootElement.checks.isDisplayed()
    }
}
