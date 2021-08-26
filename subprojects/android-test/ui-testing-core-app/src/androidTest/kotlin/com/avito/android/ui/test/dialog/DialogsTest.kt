package com.avito.android.ui.test.dialog

import com.avito.android.test.app.core.screenRule
import com.avito.android.ui.DialogsActivity
import com.avito.android.ui.test.Screen
import org.junit.Rule
import org.junit.Test
import ru.avito.util.assertThrows

class DialogsTest {

    @get:Rule
    val rule = screenRule<DialogsActivity>()

    @Test
    fun matcher_with_default_root__success__no_dialog() {
        rule.launchActivity(null)

        Screen.dialogsScreen.label.checks.isDisplayed()
    }

    @Test
    fun matcher_with_dialog_root__success__dialog_is_open() {
        rule.launchActivity(
            DialogsActivity.intent(openDialog = true)
        )
        Screen.alert.messageElement.checks.isDisplayed()
    }

    @Test
    fun matcher_with_default_root__assertion_error__dialog_is_open() {
        rule.launchActivity(
            DialogsActivity.intent(openDialog = true)
        )
        assertThrows<AssertionError> {
            Screen.dialogsScreen.label.checks.isDisplayed()
        }
    }
}
