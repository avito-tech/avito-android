package com.avito.android.test.espresso.action

import android.app.Activity
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.view.View
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.ViewMatchers
import com.avito.android.test.util.getCurrentActivity
import org.hamcrest.Matcher

/**
 * An Espresso ViewAction that changes the orientation of the screen
 */
class OrientationChangeAction(private val orientation: Int? = null) : ViewAction {

    override fun getConstraints(): Matcher<View> = ViewMatchers.isRoot()

    override fun getDescription(): String = when (orientation) {
        null -> "toggle orientation"
        else -> "change orientation to $orientation"
    }

    override fun perform(uiController: UiController, view: View) {
        uiController.loopMainThreadUntilIdle()

        with(getCurrentActivity()) {
            requestedOrientation = orientation ?: decideOrientationToToggle(this)
        }

        uiController.loopMainThreadUntilIdle()
    }

    private fun decideOrientationToToggle(activity: Activity): Int =
        when (val currentOrientation = activity.resources.configuration.orientation) {
            Configuration.ORIENTATION_PORTRAIT -> ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            Configuration.ORIENTATION_LANDSCAPE -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            else -> throw IllegalStateException("Unsupported orientation: $currentOrientation")
        }

    companion object {

        fun orientationLandscape(): ViewAction =
            OrientationChangeAction(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)

        fun orientationPortrait(): ViewAction =
            OrientationChangeAction(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)

        fun toggle(): ViewAction = OrientationChangeAction()
    }
}
