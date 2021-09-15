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

abstract class BaseScreenChecks<out T : Screen>(
    protected val screen: T,
) : ScreenChecks {

    /**
     * To avoid recursion when client invokes [isScreenOpened] directly with enabled [checkOnEachScreenInteraction].
     * [isScreenOpened] will be invoked on each view element interaction.
     */
    private var inChecking = false

    // TODO: make final after removing StrictScreenChecks (MBS-11808)
    @CallSuper
    override fun isScreenOpened() {
        if (!inChecking) {
            inChecking = true
            screenOpenedCheck()
            inChecking = false
        }
    }

    protected abstract fun screenOpenedCheck()
}

// TODO: Remove after migrating clients from bare PageObject to screens (MBS-11808)
@Deprecated("Use other implementations of BaseScreenChecks from page objects: SimpleScreen, DialogScreen, ...")
open class StrictScreenChecks<out T : Screen>(
    screen: T,
    override val checkOnEachScreenInteraction: Boolean = true
) : BaseScreenChecks<T>(screen) {

    override fun screenOpenedCheck() {
        screen.checkRootId()
    }
}

/**
 * Known limitations: doesn't work in Android 11 in case of multiple windows (dialogs, popups, ...)
 * Use other implementations of [ScreenChecks]
 */
private fun Screen.checkRootId() {
    if (this.rootId != UNKNOWN_ROOT_ID) {
        val rootElement = ViewElement(ViewMatchers.withId(this.rootId))
        rootElement.checks.exists()
        rootElement.checks.isDisplayed()
    }
}
