package com.avito.android.test.page_object

import android.view.View
import androidx.appcompat.widget.SwitchCompat
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.ViewMatchers
import com.avito.android.test.InteractionContext
import com.avito.android.test.action.ActionsDriver
import org.hamcrest.Matcher

public class SwitchElement(
    interactionContext: InteractionContext
) :
    ViewElement(interactionContext),
    SwitchElementActions by SwitchElementActionsImpl(interactionContext)

public interface SwitchElementActions {

    public fun setIsChecked(isChecked: Boolean)
}

internal class SwitchElementActionsImpl(private val driver: ActionsDriver) : SwitchElementActions {

    override fun setIsChecked(isChecked: Boolean) {
        driver.perform(SetSwitchIsCheckedAction(isChecked))
    }
}

internal class SetSwitchIsCheckedAction(private val isChecked: Boolean) : ViewAction {

    override fun getConstraints(): Matcher<View> =
        ViewMatchers.isAssignableFrom(SwitchCompat::class.java)

    override fun getDescription(): String = "Switch state to $isChecked"

    override fun perform(uiController: UiController, view: View) {
        (view as SwitchCompat).isChecked = isChecked
    }
}
