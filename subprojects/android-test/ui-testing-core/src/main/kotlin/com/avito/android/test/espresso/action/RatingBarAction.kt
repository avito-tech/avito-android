package com.avito.android.test.espresso.action

import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.ViewMatchers
import androidx.appcompat.widget.AppCompatRatingBar
import android.view.View
import org.hamcrest.Matcher

class RatingBarAction(private val rating: Float) : ViewAction {

    override fun getDescription() = "select rating"

    override fun getConstraints(): Matcher<View> = ViewMatchers.isAssignableFrom(AppCompatRatingBar::class.java)

    override fun perform(uiController: UiController, view: View) {
        val ratingBar = view as AppCompatRatingBar
        ratingBar.rating = rating
        uiController.loopMainThreadUntilIdle()
    }

}
