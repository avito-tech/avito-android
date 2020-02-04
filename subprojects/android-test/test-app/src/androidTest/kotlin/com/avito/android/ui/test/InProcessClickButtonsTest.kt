package com.avito.android.ui.test

import com.avito.android.runner.UITestFrameworkPerformException
import com.avito.android.test.UITestConfig
import com.avito.android.test.util.ClicksTypeRule
import com.avito.android.ui.ButtonsActivity
import org.junit.Ignore
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

    @Test(expected = UITestFrameworkPerformException::class)
    fun clickOnDisabledButton_mustThrowPerformException() {
        Screen.buttons.disabledButton.click()
    }

    @Test(expected = UITestFrameworkPerformException::class)
    fun longClickOnDisabledButton_mustThrowPerformException() {
        Screen.buttons.disabledButton.longClick()
    }

    @Test(expected = UITestFrameworkPerformException::class)
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

    @Test(expected = UITestFrameworkPerformException::class)
    fun longClickOnNonLongClickableButton_mustThrowPerformException() {
        Screen.buttons.nonLongClickableButton.longClick()
    }

    @Test
    fun longClickInsideLongClickableContainer_performs() {
        Screen.buttons.longClickableContainerInnerButton.longClick()

        Screen.buttons.longClickableContainerIndicator.checks.isVisible()
    }

    @Test
    fun button_with_overlapped_click_coordinates_by_non_clickable_element_can_clicked() {
        Screen.buttons.overlappedByNonClickable.click()
    }

    @Test(expected = UITestFrameworkPerformException::class)
    fun button_with_overlapped_click_coordinates_by_clickable_element_cant_clicked() {
        Screen.buttons.overlappedByClickable.click()
    }
}
