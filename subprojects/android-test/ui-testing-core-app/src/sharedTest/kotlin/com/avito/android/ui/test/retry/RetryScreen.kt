package com.avito.android.ui.test.retry

import android.view.View
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.avito.android.test.InteractionContext
import com.avito.android.test.action.Actions
import com.avito.android.test.action.ActionsDriver
import com.avito.android.test.espresso.EspressoActions
import com.avito.android.test.page_object.SimpleScreen
import com.avito.android.test.page_object.ViewElement
import com.avito.android.ui.R
import org.hamcrest.Matcher
import org.hamcrest.Matchers

class RetryScreen : SimpleScreen() {

    override val rootId: Int = R.id.activity_retry

    val button: FailViewElement = element(withId(R.id.button))
    val buttonClickIndicator: ViewElement = element(
        withId(R.id.button_click_indicator)
    )
}

class FailViewElement(
    interactionContext: InteractionContext
) : ViewElement(interactionContext) {

    private lateinit var exception: Throwable

    override val actions: Actions
        get() = FailActions(interactionContext, super.actions, exception)

    fun firstFail(exception: Throwable): ViewElement {
        this.exception = exception
        return this
    }

    class OneTimeFailedAction(private val exception: Throwable) : ViewAction {

        private var fired = false

        override fun getDescription(): String = "failed action"

        override fun getConstraints(): Matcher<View> = Matchers.allOf()

        override fun perform(uiController: UiController?, view: View?) {
            if (!fired) {
                fired = true
                throw exception
            }
        }
    }

    class FailActions(
        private val driver: ActionsDriver,
        private val delegate: Actions,
        private val exception: Throwable
    ) : Actions by delegate {

        override fun click() {
            driver.perform(
                OneTimeFailedAction(exception),
                EspressoActions.scrollIfPossible(),
                EspressoActions.click()
            )
        }
    }
}
