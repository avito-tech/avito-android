package com.avito.android.test.espresso.action

import android.content.res.Resources
import android.graphics.Rect
import android.os.SystemClock
import android.view.InputDevice
import android.view.MotionEvent
import android.view.MotionEvent.PointerProperties
import android.view.View
import android.view.ViewConfiguration
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.Configurator
import androidx.test.uiautomator.UiDevice
import org.hamcrest.Matcher

class TouchOutsideAction : ViewAction {

    private val uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

    override fun getConstraints(): Matcher<View> = ViewMatchers.isAssignableFrom(View::class.java)

    override fun getDescription(): String = "Press outside of view"

    override fun perform(uiController: UiController, view: View) {
        val rect = Rect()
        view.getGlobalVisibleRect(rect)

        //Take left point of our view and add few dp to make sure that point located outside of view
        val y = uiDevice.displayHeight.toFloat() / 2
        val x = uiDevice.displayWidth / 2f + rect.width() / 2f + HORIZONTAL_OFFSET_DP.dp()

        val events = createClickEvents(x, y)

        view.rootView.let {
            for (event in events) {
                it.dispatchTouchEvent(event)
            }
        }

        val duration: Int = ViewConfiguration.getPressedStateDuration()
        // ensures that all work enqueued to process the tap has been run.
        if (duration > 0) {
            uiController.loopMainThreadForAtLeast(duration.toLong())
        }
    }

    private fun createClickEvents(x: Float, y: Float): List<MotionEvent> {
        val properties = PointerProperties()
        properties.id = 0
        properties.toolType = Configurator.getInstance().toolType

        val cords = MotionEvent.PointerCoords()
        cords.pressure = 1f
        cords.size = 1f
        cords.x = x
        cords.y = y

        val cur = SystemClock.uptimeMillis()

        //Create two events which emulate click. First one then user put finger down. Second when user put finger up.
        //Therefore event time for MotionEvent.ACTION_UP must be bigger than time for MotionEvent.ACTION_DOWN
        //states,flags,ids sets to 0 because we don't need them in this case
        return listOf(
            MotionEvent.obtain(
                cur,
                cur + 100,
                MotionEvent.ACTION_DOWN, 1, arrayOf(properties), arrayOf(cords),
                0, 0, 1.0f, 1.0f, 0, 0, InputDevice.SOURCE_TOUCHSCREEN, 0
            ),
            MotionEvent.obtain(
                cur,
                cur + 200,
                MotionEvent.ACTION_UP, 1, arrayOf(properties), arrayOf(cords),
                0, 0, 1.0f, 1.0f, 0, 0, InputDevice.SOURCE_TOUCHSCREEN, 0
            )
        )
    }

}

@Suppress("unused")
private fun Int.dp() = Resources.getSystem().displayMetrics.density * this

private const val HORIZONTAL_OFFSET_DP = 15
