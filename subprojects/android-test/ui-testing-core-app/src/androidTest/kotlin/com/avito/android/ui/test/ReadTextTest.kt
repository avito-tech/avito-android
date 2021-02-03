package com.avito.android.ui.test

import android.widget.EditText
import com.avito.android.test.app.core.screenRule
import com.avito.android.ui.EditTextActivity
import com.avito.android.ui.R
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException

class ReadTextTest {

    @get:Rule
    val rule = screenRule<EditTextActivity>()

    @Suppress("DEPRECATION")
    @get:Rule
    val exception: ExpectedException = ExpectedException.none()

    @Test
    fun readNonBlankText_allowBlankIsFalse() = with(rule) {
        launchActivity(null)

        rule.runOnUiThread {
            activity.findViewById<EditText>(R.id.edit_text).setText("my_custom_text")
        }

        val captured = Screen.editTextScreen.editText.read(allowBlank = false)
        Assert.assertEquals("my_custom_text", captured)
    }

    @Test
    fun readBlankText_allowBlankIsFalse() = with(rule) {
        launchActivity(null)

        rule.runOnUiThread {
            activity.findViewById<EditText>(R.id.edit_text).setText("")
        }

        exception.expectMessage(
            "read() waited, but view.text still has empty string value; " +
                "use read(allowBlank=true) if you really need it"
        )
        Screen.editTextScreen.editText.read(allowBlank = false)
        Unit
    }

    @Test
    fun readBlankText_allowBlankIsTrue() = with(rule) {
        launchActivity(null)

        rule.runOnUiThread {
            activity.findViewById<EditText>(R.id.edit_text).setText("")
        }

        val captured = Screen.editTextScreen.editText.read(allowBlank = true)
        Assert.assertEquals("", captured)
    }

    @Test
    fun readText_with_delay() = with(rule) {
        launchActivity(null)

        rule.runOnUiThread {
            activity.findViewById<EditText>(R.id.edit_text).run {
                setText("")
                postDelayed({ setText("delayedText") }, 1000)
            }
        }

        val captured = Screen.editTextScreen.editText.read(allowBlank = false)
        Assert.assertEquals("delayedText", captured)
    }

    // todo recycler view tests
}
