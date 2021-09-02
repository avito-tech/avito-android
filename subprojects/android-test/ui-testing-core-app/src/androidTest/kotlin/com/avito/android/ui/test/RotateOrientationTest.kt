package com.avito.android.ui.test

import androidx.test.platform.app.InstrumentationRegistry
import com.avito.android.test.Device
import com.avito.android.test.app.core.screenRule
import com.avito.android.ui.DialogsActivity
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test

class RotateOrientationTest {

    @get:Rule
    val rule = screenRule<DialogsActivity>()

    @Test
    fun rotate__changes_orientation() {
        rule.launchActivity(null)

        verifyRotationBehaviour()
    }

    @Test
    fun rotate__changes_orientation__with_dialog() {
        rule.launchActivity(
            DialogsActivity.intent(openDialog = true)
        )
        verifyRotationBehaviour()
    }

    @Test
    fun rotate__changes_orientation__with_popup_window() {
        rule.launchActivity(
            DialogsActivity.intent(openPopup = true)
        )
        verifyRotationBehaviour()
    }

    private fun verifyRotationBehaviour() {
        val initialOrientation = orientation()

        Device.rotate()

        val newOrientation = orientation()

        assertThat(initialOrientation).isNotEqualTo(newOrientation)
    }

    private fun orientation(): Int =
        InstrumentationRegistry.getInstrumentation().targetContext.resources.configuration.orientation
}
