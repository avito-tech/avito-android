package com.avito.android.test.page_object

import android.app.Activity
import android.graphics.Rect
import android.view.View
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.platform.app.InstrumentationRegistry
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`

class KeyboardElement : PageObject() {

    val actions = ActionLibrary()

    val checks = CheckLibrary()

    fun close() {
        Espresso.closeSoftKeyboard()
    }

    class ActionLibrary {

        fun pressImeAction(element: PageObjectElement) {
            element.interactionContext.perform(ViewActions.pressImeActionButton())
        }
    }

    /**
     * WARNING: we can't check keyboard status after rotating screen using programmatically way.
     * For this case, we can't detect keyboard open status and always returns false for
     * isDisplayed method and true for isNotDisplayed method.
     *
     * https://issuetracker.google.com/u/1/issues/68137674
     */
    class CheckLibrary {
        companion object {
            // not a bad name for private const
            @Suppress("VariableMaxLength")
            private const val KEYBOARD_MINIMUM_HEIGHT_PERCENTAGE = .30f
        }

        fun isDisplayed(activity: Activity) {
            // FIXME(MBS-1301)
            Thread.sleep(1000)
            checkDisplayed(activity, true)
        }

        fun isNotDisplayed(activity: Activity) {
            // FIXME(MBS-1301)
            Thread.sleep(1000)
            checkDisplayed(activity, false)
        }

        private fun checkDisplayed(activity: Activity, isOpen: Boolean) {
            var threshold = 0
            var activityEffectiveHeight = 0

            InstrumentationRegistry.getInstrumentation().runOnMainSync {
                val content = activity.findViewById<View>(android.R.id.content)

                val activityHeight = content.rootView.height
                val minimalKeyboardHeight = activityHeight * KEYBOARD_MINIMUM_HEIGHT_PERCENTAGE

                // If mAttachInfo is null we can't check keyboard position.
                // https://issuetracker.google.com/u/1/issues/68137674
                View::class.java.getDeclaredField("mAttachInfo").apply {
                    isAccessible = true

                    if (get(content) == null) {
                        throw RuntimeException(
                            "Can't check keyboard position. Because View::mAttachInfo is null." +
                                " Did you rotate screen in test before this check?"
                        )
                    }
                }

                activityEffectiveHeight = Rect()
                    .apply { content.getWindowVisibleDisplayFrame(this) }
                    .height()

                threshold = (activityHeight - minimalKeyboardHeight).toInt()
            }

            assertThat(
                generateErrorMessage(
                    displayed = isOpen,
                    activityEffectiveHeight = activityEffectiveHeight,
                    threshold = threshold
                ),
                activityEffectiveHeight <= threshold,
                `is`(isOpen)
            )
        }

        private fun generateErrorMessage(
            displayed: Boolean,
            activityEffectiveHeight: Int,
            threshold: Int
        ): String {
            val getStatusMessage: (isDisplayed: Boolean, withSizes: Boolean) -> String =
                { isDisplayed, withSizes ->
                    val status = if (isDisplayed) {
                        "is displayed"
                    } else {
                        "is not displayed"
                    }
                    val sizes =
                        " actualEffectiveHeight: $activityEffectiveHeight " +
                            "thresholdEffectiveHeight: $threshold"

                    "(keyboard $status on the screen.${if (withSizes) sizes else ""})"
                }

            val doesNotMatch = "doesn't match the selected view."
            val expected = "Expected:"

            return "${getStatusMessage(!displayed, true)} $doesNotMatch" +
                " $expected ${getStatusMessage(displayed, false)}"
        }
    }
}
