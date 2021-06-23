package com.avito.android.ui.test

import android.view.WindowManager
import com.avito.android.rule.InHouseScenarioScreenRule
import com.avito.android.test.Device
import com.avito.android.ui.KeyboardActivity
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import ru.avito.util.assertThrows

class KeyboardTest {

    @get:Rule
    val rule = object : InHouseScenarioScreenRule<KeyboardActivity>(KeyboardActivity::class.java) {}

    @Before
    fun before() {
        rule.launchActivity(null)
    }

    @Test
    fun openActivity_keyboardIsNotDisplayed_whenAdjustUnspecified() {
        setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_UNSPECIFIED)
        Device.keyboard.checks.isNotDisplayed()
    }

    @Test
    fun openActivity_keyboardIsNotDisplayed_whenAdjustNothing() {
        setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
        Device.keyboard.checks.isNotDisplayed()
    }

    @Test
    fun openActivity_keyboardIsNotDisplayed_whenAdjustPan() {
        setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        Device.keyboard.checks.isNotDisplayed()
    }

    @Test
    fun openActivity_keyboardIsNotDisplayed_whenAdjustResize() {
        setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        Device.keyboard.checks.isNotDisplayed()
    }

    @Test
    fun openKeyboardUsingClick_keyboardIsDisplayed_whenAdjustUnspecified() {
        setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_UNSPECIFIED)
        checkOpenedUsingClickOnInput()
    }

    @Test
    fun openKeyboardUsingClick_keyboardIsDisplayed_whenAdjustNothing() {
        setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
        checkOpenedUsingClickOnInput()
    }

    @Test
    fun openKeyboardUsingClick_keyboardIsDisplayed_whenAdjustPan() {
        setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        checkOpenedUsingClickOnInput()
    }

    @Test
    fun openKeyboardUsingClick_keyboardIsDisplayed_whenAdjustResize() {
        setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        checkOpenedUsingClickOnInput()
    }

    @Test
    fun openKeyboardUsingInputMethod_keyboardIsDisplayed_whenAdjustUnspecified() {
        setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_UNSPECIFIED)
        checkOpenedUsingInputMethod()
    }

    @Test
    fun openKeyboardUsingInputMethod_keyboardIsDisplayed_whenAdjustNothing() {
        setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
        checkOpenedUsingInputMethod()
    }

    @Test
    fun openKeyboardUsingInputMethod_keyboardIsDisplayed_whenAdjustPan() {
        setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        checkOpenedUsingInputMethod()
    }

    @Test
    fun openKeyboardUsingInputMethod_keyboardIsDisplayed_whenAdjustResize() {
        setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        checkOpenedUsingInputMethod()
    }

    @Test
    fun closeKeyboardUsingEspresso_keyboardIsNotDisplayed_whenAdjustUnspecified() {
        setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_UNSPECIFIED)
        checkClosedUsingEspresso()
    }

    @Test
    fun closeKeyboardUsingEspresso_keyboardIsNotDisplayed_whenAdjustNothing() {
        setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
        checkClosedUsingEspresso()
    }

    @Test
    fun closeKeyboardUsingEspresso_keyboardIsNotDisplayed_whenAdjustPan() {
        setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        checkClosedUsingEspresso()
    }

    @Test
    fun closeKeyboardUsingEspresso_keyboardIsNotDisplayed_whenAdjustResize() {
        setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        checkClosedUsingEspresso()
    }

    @Test
    fun closeKeyboardUsingInputMethod_keyboardIsNotDisplayed_whenAdjustUnspecified() {
        setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_UNSPECIFIED)
        checkClosedUsingInputMethod()
    }

    @Test
    fun closeKeyboardUsingInputMethod_keyboardIsNotDisplayed_whenAdjustNothing() {
        setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
        checkClosedUsingInputMethod()
    }

    @Test
    fun closeKeyboardUsingInputMethod_keyboardIsNotDisplayed_whenAdjustPan() {
        setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        checkClosedUsingInputMethod()
    }

    @Test
    fun closeKeyboardUsingInputMethod_keyboardIsNotDisplayed_whenAdjustResize() {
        setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        checkClosedUsingInputMethod()
    }

    @Test
    fun keyboardChecksAfterRotations_succeed_whenAdjustUnspecified() {
        setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_UNSPECIFIED)
        checkRotations()
    }

    @Test
    fun keyboardChecksAfterRotations_succeed_whenAdjustNothing() {
        setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
        checkRotations()
    }

    @Test
    fun keyboardChecksAfterRotations_succeed_whenAdjustPan() {
        setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        checkRotations()
    }

    @Test
    fun keyboardChecksAfterRotations_succeed_whenAdjustResize() {
        setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        checkRotations()
    }

    @Test
    fun isDisplayed_throwsAssertionError_whenKeyboardIsClosed() {
        assertThrows<AssertionError> { Device.keyboard.checks.isDisplayed() }
    }

    @Test
    fun isNotDisplayed_throwsAssertionError_whenKeyboardIsOpened() {
        openKeyboard()
        assertThrows<AssertionError> { Device.keyboard.checks.isNotDisplayed() }
    }

    private fun checkOpenedUsingClickOnInput() {
        Screen.keyboardScreen.input.actions.click()
        Device.keyboard.checks.isDisplayed()
    }

    private fun checkOpenedUsingInputMethod() {
        openKeyboard()
        Device.keyboard.checks.isDisplayed()
    }

    private fun checkClosedUsingEspresso() {
        openKeyboard()
        Device.keyboard.close()
        Device.keyboard.checks.isNotDisplayed()
    }

    private fun checkClosedUsingInputMethod() {
        openKeyboard()
        hideKeyboard()
        Device.keyboard.checks.isNotDisplayed()
    }

    private fun checkRotations() {
        openKeyboard()
        Device.rotate()
        Device.keyboard.checks.isNotDisplayed() // Keyboard closes after rotation

        openKeyboard()
        Device.keyboard.checks.isDisplayed()

        Device.rotate()
        Device.keyboard.checks.isNotDisplayed()
    }

    private fun setSoftInputMode(mode: Int) {
        rule.scenario.onActivity { activity ->
            activity.window.setSoftInputMode(mode)
        }
    }

    private fun openKeyboard() {
        rule.scenario.onActivity {
            require(it is KeyboardActivity)
            it.openKeyboard()
        }
    }

    private fun hideKeyboard() {
        rule.scenario.onActivity {
            require(it is KeyboardActivity)
            it.hideKeyboard()
        }
    }
}
