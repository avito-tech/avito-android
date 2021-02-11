package com.avito.android.ui.test

import androidx.test.espresso.PerformException
import com.avito.android.test.UITestConfig
import com.avito.android.test.app.core.screenRule
import com.avito.android.test.util.ClicksTypeRule
import com.avito.android.ui.ButtonsActivity
import org.junit.Rule
import org.junit.Test

class InProcessClickButtonsTest {

    @get:Rule
    val rule = screenRule<ButtonsActivity>(launchActivity = true)

    @get:Rule
    val clickRule = ClicksTypeRule(clickType = UITestConfig.ClickType.InProcessClick)

    @Test
    fun clickOnEnabledButton_performed() {
        Screen.buttons.enabledButton.click()

        Screen.buttons.enabledButtonClickIndicatorView.checks.isDisplayed()
        Screen.buttons.enabledButtonLongClickIndicatorView.checks.isNotDisplayed()
    }

    @Test
    fun longClickOnEnabledButton_performed() {
        Screen.buttons.enabledButton.longClick()

        Screen.buttons.enabledButtonClickIndicatorView.checks.isNotDisplayed()
        Screen.buttons.enabledButtonLongClickIndicatorView.checks.isDisplayed()
    }

    @Test(expected = PerformException::class)
    fun clickOnDisabledButton_mustThrowPerformException() {
        Screen.buttons.disabledButton.click()
    }

    @Test(expected = PerformException::class)
    fun longClickOnDisabledButton_mustThrowPerformException() {
        Screen.buttons.disabledButton.longClick()
    }

    @Test(expected = PerformException::class)
    fun clickOnNonClickableButton_mustThrowPerformException() {
        Screen.buttons.nonClickableButton.click()
    }

    @Test
    fun clickInsideClickableContainer_performs() {
        Screen.buttons.clickableContainerInnerButton.checks.isNotClickable()
        Screen.buttons.clickableContainerInnerButton.click()

        Screen.buttons.clickableContainerIndicator.checks.isVisible()
    }

    @Test
    fun clickOnNonLongClickableButton_performed() {
        Screen.buttons.nonLongClickableButton.checks.isClickable()
        Screen.buttons.nonLongClickableButton.click()

        Screen.buttons.nonLongClickableButtonIndicator.checks.isDisplayed()
    }

    @Test(expected = PerformException::class)
    fun longClickOnNonLongClickableButton_mustThrowPerformException() {
        Screen.buttons.nonLongClickableButton.longClick()
    }

    @Test
    fun longClickInsideLongClickableContainer_performs() {
        Screen.buttons.longClickableContainerInnerButton.longClick()

        Screen.buttons.longClickableContainerIndicator.checks.isVisible()
    }

    @Test(expected = PerformException::class)
    fun hiddenButton_fails() {
        Screen.buttons.hiddenButton.click()

        Screen.buttons.hiddenButtonIndicator.checks.isDisplayed()
    }

    @Test
    fun animatedView_performs() {
        Screen.buttons.animatedView.click()

        Screen.buttons.animatedViewIndicator.checks.isDisplayed()
    }
}
