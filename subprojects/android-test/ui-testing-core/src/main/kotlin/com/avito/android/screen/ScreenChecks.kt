package com.avito.android.screen

import androidx.annotation.CallSuper

/**
 * MBS-7204 Screenchecks Story
 */
interface ScreenChecks {

    val checkOnEachScreenInteraction: Boolean
        get() = false

    fun isScreenOpened()
}

abstract class BaseScreenChecks<out T : Screen>(
    protected val screen: T,
) : ScreenChecks {

    /**
     * To avoid recursion when client invokes [isScreenOpened] directly with enabled [checkOnEachScreenInteraction].
     * [isScreenOpened] will be invoked on each view element interaction.
     */
    private var inChecking = false

    @CallSuper
    final override fun isScreenOpened() {
        if (!inChecking) {
            inChecking = true
            screenOpenedCheck()
            inChecking = false
        }
    }

    protected abstract fun screenOpenedCheck()
}
