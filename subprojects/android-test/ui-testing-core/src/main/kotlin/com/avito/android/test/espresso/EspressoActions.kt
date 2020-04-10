package com.avito.android.test.espresso

import android.view.InputDevice
import android.view.MotionEvent
import android.view.View
import androidx.test.espresso.PerformException
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.GeneralClickAction
import androidx.test.espresso.action.GeneralSwipeAction
import androidx.test.espresso.action.PrecisionDescriber
import androidx.test.espresso.action.Press
import androidx.test.espresso.action.Swipe
import androidx.test.espresso.action.SwipeDirection
import androidx.test.espresso.action.Swiper
import androidx.test.espresso.action.Tap
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.actionWithAssertions
import androidx.test.espresso.action.CoordinatesProvider
import com.avito.android.test.UITestConfig
import com.avito.android.test.element.field.actions.TypeText
import com.avito.android.test.espresso.action.ActionOnClickableElement
import com.avito.android.test.espresso.action.ActionOnEnabledElement
import com.avito.android.test.espresso.action.ActionOnLongClickableElement
import com.avito.android.test.espresso.action.WaitForIdleAction
import com.avito.android.test.espresso.action.click.inProcessClickAction
import com.avito.android.test.espresso.action.click.inProcessLongClickAction
import com.avito.android.test.espresso.action.scroll.ScrollToIfPossibleAction
import org.hamcrest.Matcher
import org.hamcrest.core.IsAnything

// TODO: make internal
object EspressoActions {

    fun typeText(stringToBeTyped: String): ViewAction =
        actionWithAssertions(TypeText(stringToBeTyped))

    /**
     * Enables scrolling to the given view.
     * Less strict version of ScrollToAction
     * If view not a descendant of a ScrollView - nothing happens
     */
    fun scrollIfPossible(): ViewAction = actionWithAssertions(ScrollToIfPossibleAction())

    /**
     * Hack: waits for main thread to be idle
     */
    fun waitForIdle(): ViewAction = actionWithAssertions(WaitForIdleAction())

    /**
     * Expose espresso API
     */
    fun swipe(
        direction: SwipeDirection,
        speed: Swiper = Swipe.FAST,
        precision: PrecisionDescriber = Press.FINGER
    ): ViewAction {
        val providers = direction.toCoordinateProvider()
        return actionWithAssertions(
            GeneralSwipeAction(
                speed,
                providers.first,
                providers.second,
                precision
            )
        )
    }

    fun click(
        type: UITestConfig.ClickType = UITestConfig.clicksType,
        coordinatesProvider: CoordinatesProvider = VisibleCenterGlobalCoordinatesProvider(),
        visualize: Boolean = UITestConfig.visualizeClicks
    ): ViewAction {

        fun safeAction(action: ViewAction) = ActionOnEnabledElement(
            ActionOnClickableElement(action)
        )

        val clickAction = when (type) {
            is UITestConfig.ClickType.EspressoClick -> when (type.rollbackPolicy) {

                is UITestConfig.ClickType.EspressoClick.ClickRollbackPolicy.DoNothing -> defaultEspressoClickAction(
                    coordinatesProvider
                )

                is UITestConfig.ClickType.EspressoClick.ClickRollbackPolicy.TryOneMoreClick -> {
                    val rollbackAction = safeAction(defaultEspressoClickAction(coordinatesProvider))
                    return defaultEspressoClickAction(coordinatesProvider, rollbackAction)
                }

                is UITestConfig.ClickType.EspressoClick.ClickRollbackPolicy.Fail -> defaultEspressoClickAction(
                    coordinatesProvider, object : ViewAction {
                        override fun getDescription(): String =
                            "fake fail action after click interpreted as long click"

                        override fun getConstraints(): Matcher<View> = IsAnything()

                        override fun perform(uiController: UiController?, view: View?) {
                            throw PerformException.Builder()
                                .withActionDescription("click interpreted as long click")
                                .withViewDescription("view")
                                .build()
                        }
                    }
                )
            }

            is UITestConfig.ClickType.InProcessClick -> inProcessClickAction(
                coordinatesProvider = coordinatesProvider,
                visualizeClicks = visualize
            )
        }
        return safeAction(clickAction)
    }

    fun longClick(
        type: UITestConfig.ClickType = UITestConfig.clicksType,
        coordinatesProvider: CoordinatesProvider = VisibleCenterGlobalCoordinatesProvider(),
        visualize: Boolean = UITestConfig.visualizeClicks
    ): ViewAction {
        fun safeAction(action: ViewAction) = ActionOnEnabledElement(
            ActionOnLongClickableElement(action)
        )
        return safeAction(
            when (type) {
                is UITestConfig.ClickType.EspressoClick -> ViewActions.longClick()
                is UITestConfig.ClickType.InProcessClick -> inProcessLongClickAction(coordinatesProvider, visualize)
            }
        )
    }

    /**
     * Same as [ViewActions.click] but with usage of given coordinates provider
     */
    private fun defaultEspressoClickAction(
        coordinatesProvider: CoordinatesProvider,
        rollbackAction: ViewAction? = null
    ): ViewAction =
        actionWithAssertions(
            GeneralClickAction(
                Tap.SINGLE,
                coordinatesProvider,
                Press.FINGER,
                InputDevice.SOURCE_UNKNOWN,
                MotionEvent.BUTTON_PRIMARY,
                rollbackAction
            )
        )
}
