package com.avito.android.test.page_object

import android.view.View
import android.widget.ProgressBar
import androidx.test.espresso.ViewAssertion
import com.avito.android.test.InteractionContext
import com.avito.android.test.SimpleInteractionContext
import com.avito.android.test.checks.Checks
import com.avito.android.test.checks.ChecksDriver
import com.avito.android.test.checks.ChecksImpl
import junit.framework.AssertionFailedError
import org.hamcrest.Matcher

class ProgressBarElement(
    interactionContext: InteractionContext
) : ViewElement(interactionContext) {

    override val checks: ProgressBarChecks = ProgressBarChecksImpl(interactionContext)

    // TODO: remove this constructor and use element fabric method to create an instance
    constructor(matcher: Matcher<View>) : this(SimpleInteractionContext(matcher))
}

interface ProgressBarChecks : Checks {

    fun withProgress(progress: Int)
}

class ProgressBarChecksImpl(
    private val driver: ChecksDriver
) : ProgressBarChecks,
    Checks by ChecksImpl(driver) {

    override fun withProgress(progress: Int) {
        driver.check(
            ViewAssertion { view, _ ->
                when (view) {
                    is ProgressBar ->
                        if (view.progress != progress) {
                            throw AssertionFailedError(
                                "Current progress is: ${view.progress}. Checked is $progress"
                            )
                        }
                    else -> throw AssertionFailedError("Matched view with is not ProgressBar")
                }
            }
        )
    }
}
