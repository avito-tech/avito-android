package com.avito.android.screen

import android.annotation.SuppressLint
import androidx.annotation.CallSuper
import androidx.test.espresso.matcher.ViewMatchers
import com.avito.android.screen.Screen.Companion.UNKNOWN_ROOT_ID
import com.avito.android.test.page_object.ViewElement

/**
 * MBS-7204 Screenchecks Story
 */
interface ScreenChecks {

    @Deprecated("Will be deleted in 2020.9")
    val screen: Screen

    val checkOnEachScreenInteraction: Boolean
        get() = false

    @Deprecated("Use isScreenOpened() instead. Will be deleted in 2020.9",
        replaceWith = ReplaceWith("isScreenOpened()")
    )
    @CallSuper
    fun isOpened() {
        screen.checkRootId()
    }

    fun isScreenOpened() {

    }
}

open class StrictScreenChecks(
    override val screen: Screen,
    override val checkOnEachScreenInteraction: Boolean = true
) : ScreenChecks {

    @CallSuper
    override fun isScreenOpened() {
        screen.checkRootId()
    }

    @SuppressLint("MissingSuperCall")
    final override fun isOpened() {
        isScreenOpened()
    }
}

private fun Screen.checkRootId() {
    if (this.rootId != UNKNOWN_ROOT_ID) {
        val rootElement = ViewElement(ViewMatchers.withId(this.rootId))
        rootElement.checks.exists()
        rootElement.checks.isDisplayed()
    }
}
