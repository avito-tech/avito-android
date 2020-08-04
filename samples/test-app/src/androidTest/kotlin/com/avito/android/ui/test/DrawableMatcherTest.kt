package com.avito.android.ui.test

import com.avito.android.test.app.core.screenRule
import com.avito.android.ui.DrawableActivity
import com.avito.android.ui.R
import org.junit.Rule
import org.junit.Test

class DrawableMatcherTest {

    @get:Rule
    val rule = screenRule<DrawableActivity>(launchActivity = true)

    @Test
    fun backgroundDrawable_matches_colorResource() {
        Screen.drawablesScreen.viewWithBackgroundColor.checks.hasBackground(R.color.red)
    }

    @Test
    fun backgroundDrawable_matches_drawable() {
        Screen.drawablesScreen.viewWithBackgroundImage.checks
            .hasBackground(R.drawable.ic_check_black_24dp)
    }

    @Test
    fun backgroundDrawable_matches_onlyTint() {
        Screen.drawablesScreen.viewWithBackgroundImageWithTint.checks
            .hasBackground(tint = android.R.color.white)
    }

    @Test
    fun drawable_matches_sourceDrawable() {
        Screen.drawablesScreen.imageView.checks
            .withSourceDrawable(R.drawable.ic_check_black_24dp)
    }

    @Test
    fun drawableWithTint_matches_sourceDrawable() {
        Screen.drawablesScreen.imageViewWithTint.checks
            .withSourceDrawable(R.drawable.ic_check_black_24dp)
    }

    @Test
    fun drawableWithTint_matches_sourceDrawableWithTint() {
        Screen.drawablesScreen.imageViewWithTint.checks
            .withSourceDrawable(R.drawable.ic_check_black_24dp, android.R.color.white)
    }
}
