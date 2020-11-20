package com.avito.android.test.element.field.actions

import android.app.Application
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.platform.app.InstrumentationRegistry
import com.avito.android.test.espresso.EspressoActions
import com.avito.android.test.util.executeMethod
import com.avito.android.test.util.getFieldByReflectionWithAnyField
import com.avito.android.waiter.waitFor
import me.weishu.reflection.Reflection
import org.hamcrest.Matcher
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import java.util.concurrent.TimeUnit

/**
 * Type text action that is used for typing instead of [androidx.test.espresso.action.TypeTextAction]
 *
 * Differences:
 *  - We wait for focus on the field
 *  - We wait for at least one text changed event
 *  - We use InputConnection API (for software keyboards) instead of injecting key events to
 *    (window or input managers)
 *
 * Why keyboard API is better?
 *  - We can apply any symbol as text instead of injecting low level key events.
 *    It's hard to apply, for example, cyrillic symbols using key events API.
 *  - Actual user most often uses software keyboards
 */
internal class TypeText(private val stringToBeTyped: String) : ViewAction {

    override fun getConstraints(): Matcher<View> = Matchers.allOf(
        ViewMatchers.isDisplayed(),
        ViewMatchers.isAssignableFrom(EditText::class.java)
    )

    override fun perform(uiController: UiController, view: View) {
        view as EditText

        if (stringToBeTyped.isEmpty()) {
            return
        }

        tapForFocus(uiController = uiController, editText = view)
        writeText(uiController = uiController, editText = view)

        uiController.loopMainThreadUntilIdle()
    }

    private fun tapForFocus(uiController: UiController, editText: EditText) {
        EspressoActions.click().perform(uiController, editText)
        uiController.loopMainThreadUntilIdle()

        waitMainLoopFor(uiController) {
            assertThat(
                "View must have focus after tap before text typing",
                editText.hasFocus(),
                Matchers.`is`(true)
            )
        }
    }

    private fun writeText(uiController: UiController, editText: EditText) {
        assertThat("Hidden API is unavailable", unseal, Matchers.`is`(true))

        val context = (
            ApplicationProvider.getApplicationContext<Application>()
                .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            )
            .getFieldByReflectionWithAnyField("mIInputContext")

        var textChangedAtLeastOnce = false
        val textWatcher = object : SimpleTextWatcher() {

            override fun beforeTextChanged(
                s: CharSequence,
                start: Int,
                count: Int,
                after: Int
            ) {
                textChangedAtLeastOnce = true
            }
        }
        editText.addTextChangedListener(textWatcher)

        context.executeMethod("beginBatchEdit")
        context.executeMethod("finishComposingText")
        context.executeMethod("commitText", stringToBeTyped, 1)
        context.executeMethod("endBatchEdit")

        waitMainLoopFor(uiController) {
            assertThat(
                "Failed to write text. Typing event has sent but hasn't handled",
                textChangedAtLeastOnce,
                Matchers.`is`(true)
            )
        }
        editText.removeTextChangedListener(textWatcher)
    }

    private fun waitMainLoopFor(uiController: UiController, action: () -> Unit) = waitFor(
        frequencyMs = 100,
        timeoutMs = TimeUnit.SECONDS.toMillis(3),
        allowedExceptions = setOf(Throwable::class.java),
        sleepAction = { delay -> uiController.loopMainThreadForAtLeast(delay) },
        action = action
    )

    override fun getDescription(): String = "type text $stringToBeTyped"

    private companion object HiddenApiHack {

        /**
         * Workaround for access to restricted API.
         * https://developer.android.com/distribute/best-practices/develop/restrictions-non-sdk-interfaces
         *
         * Our public API request: https://issuetracker.google.com/issues/144891978
         */
        private val unseal: Boolean by lazy {
            0 == Reflection.unseal(InstrumentationRegistry.getInstrumentation().targetContext)
        }
    }
}

internal open class SimpleTextWatcher : TextWatcher {

    override fun afterTextChanged(s: Editable?) {}

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
}
