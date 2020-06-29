package com.avito.android.test.espresso.action

import android.view.View
import android.widget.TextView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.ViewMatchers
import com.avito.android.waiter.waitFor
import org.hamcrest.Matcher
import org.junit.Assert.assertFalse

class TextViewReadAction : ViewAction {

    companion object {

        /**
         * @param perform use suitable driver to perform this view action
         */
        fun getResult(allowBlank: Boolean, perform: (TextViewReadAction) -> Unit): String {
            val action = TextViewReadAction()
            return if (allowBlank) {
                perform(action)
                action.result ?: ""
            } else {
                // FYI: will loop inside driver's loop here
                waitFor(allowedExceptions = setOf(AssertionError::class.java)) {
                    perform(action)
                    assertFalse(
                        "read() waited, but view.text still has empty string value; " +
                                "use read(allowBlank=true) if you really need it",
                        action.result.isNullOrBlank()
                    )
                }
                action.result!!
            }
        }
    }

    private var result: String? = null

    override fun getConstraints(): Matcher<View> =
        ViewMatchers.isAssignableFrom(TextView::class.java)

    override fun getDescription(): String = "getting text from a TextView"

    override fun perform(uiController: UiController, view: View) {
        result = view.readText()
    }

    private fun View.readText(): String = (this as TextView).text.toString()
}
