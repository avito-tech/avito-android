package com.avito.android.test.page_object

import android.view.View
import android.widget.RatingBar
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.ViewAssertion
import androidx.test.espresso.matcher.ViewMatchers
import com.avito.android.test.InteractionContext
import com.avito.android.test.action.ActionsDriver
import com.avito.android.test.checks.Checks
import com.avito.android.test.checks.ChecksDriver
import com.avito.android.test.checks.ChecksImpl
import junit.framework.AssertionFailedError
import org.hamcrest.Matcher

public class RatingBarElement(
    interactionContext: InteractionContext
) : ViewElement(interactionContext),
    RatingBarActions by RatingBarActionsImpl(interactionContext) {

    override val checks: RatingBarChecks = RatingBarChecksImpl(interactionContext)
}

public interface RatingBarActions {

    public fun setRating(rating: Float)
}

internal class RatingBarActionsImpl(private val driver: ActionsDriver) : RatingBarActions {

    override fun setRating(rating: Float) {
        driver.perform(SetRatingAction(rating))
    }
}

public interface RatingBarChecks : Checks {

    public fun withRating(rating: Float)
}

internal class RatingBarChecksImpl(
    private val driver: ChecksDriver
) : RatingBarChecks,
    Checks by ChecksImpl(driver) {

    override fun withRating(rating: Float) {
        driver.check(
            ViewAssertion { view, _ ->
                when (view) {
                    is RatingBar ->
                        if (view.rating != rating) {
                            throw AssertionFailedError(
                                "Current rating is: ${view.rating}. Checked is $rating"
                            )
                        }
                    else -> throw AssertionFailedError("Matched view with is not RatingBar")
                }
            }
        )
    }
}

internal class SetRatingAction(private val rating: Float) : ViewAction {

    override fun getConstraints(): Matcher<View> =
        ViewMatchers.isAssignableFrom(RatingBar::class.java)

    override fun getDescription(): String = "Set rating for RatingBar"

    override fun perform(uiController: UiController, view: View) {
        (view as RatingBar).rating = rating
        view.numStars
    }
}
