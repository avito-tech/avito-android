package androidx.test.espresso.assertion

import androidx.test.espresso.ViewAssertion

/**
 * RecyclerView tries to scroll to view before check
 * It's is completely ok, but breaks DoesNotExistViewAssertion
 */
internal fun ViewAssertion.isDoesntExistAssertion(): Boolean =
    this is ViewAssertions.DoesNotExistViewAssertion
