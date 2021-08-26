package com.avito.android.ui.test.dialog

import androidx.test.espresso.NoMatchingRootException
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
    fun check_regular_window__success__matcher_with_default_root() {
        rule.launchActivity(null)

        Screen.dialogsScreen.label.checks.isDisplayed()
    }

    @Test
    fun check_regular_window__fail__matcher_with_dialog_root() {
        rule.launchActivity(null)

        assertThrows<NoMatchingRootException> {
            DialogScreenWithDialogRoot().label.checks.isDisplayed()
        }
    }

    @Test
    fun check_regular_window__fail__matcher_with_popup_window() {
        rule.launchActivity(null)

        assertThrows<NoMatchingRootException> {
            DialogScreenWithPopupRoot().label.checks.isDisplayed()
        }
    }

    @Test
    fun check_regular_window__fail__matcher_with_default_root_and_dialog_is_open() {
        rule.launchActivity(
            DialogsActivity.intent(openDialog = true)
        )
        // Espresso pick's wrongly dialog window
        assertThrows<AssertionError> {
            Screen.dialogsScreen.label.checks.isDisplayed()
        }
    }

    @Test
    fun check_dialog_window__success__matcher_with_dialog_root() {
        rule.launchActivity(
            DialogsActivity.intent(openDialog = true)
        )
        Screen.alert.messageElement.checks.isDisplayed()
    }

    @Test
    fun check_popup_window__success__matcher_with_popup_window() {
        rule.launchActivity(
            DialogsActivity.intent(openPopup = true)
        )
        Screen.dialogsScreen.popup.label.checks.isDisplayed()
    }
}
