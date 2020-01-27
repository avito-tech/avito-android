package com.avito.android.ui.test

import com.avito.android.ui.DrawableActivity
import com.avito.android.ui.R
import org.junit.Rule
import org.junit.Test

class BackgroundDrawableTest {

    @get:Rule
    val rule = screenRule<DrawableActivity>(launchActivity = true)

    @Test
    fun backgroundDrawable_matches_whenBackgroundIsColorResource() {
        Screen.backgroundDrawableScreen.viewWithBackgroundRedColor.checks.hasBackground(R.color.red)
    }

    @Test
    fun backgroundDrawable_matches_whenBackgroundIsIconResource() {
        Screen.backgroundDrawableScreen.viewWithBackgroundCheckIcon.checks
            .hasBackground(R.drawable.ic_check_black_24dp)
    }
}
