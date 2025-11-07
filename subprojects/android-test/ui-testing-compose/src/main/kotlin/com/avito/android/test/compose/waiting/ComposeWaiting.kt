package com.avito.android.test.compose.waiting

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.junit4.ComposeTestRule
import com.avito.android.test.compose.ComposeInteractionContext

@OptIn(ExperimentalTestApi::class)
public interface ComposeWaiting {
    public val interactionContext: ComposeInteractionContext

    public fun waitUntilNodeCount(
        count: Int,
        timeoutMillis: Long = 1_000L
    ) {
        interactionContext.composeTestRule.waitUntilNodeCount(interactionContext.filter.matcher, count, timeoutMillis)
    }

    public fun waitUntilExactlyOneExists(timeoutMillis: Long = 1_000L) {
        interactionContext.composeTestRule.waitUntilExactlyOneExists(interactionContext.filter.matcher, timeoutMillis)
    }

    public fun waitUntilAtLeastOneExists(timeoutMillis: Long = 1_000L) {
        interactionContext.composeTestRule.waitUntilAtLeastOneExists(interactionContext.filter.matcher, timeoutMillis)
    }

    public fun waitUntilDoesNotExist(timeoutMillis: Long = 1_000L) {
        interactionContext.composeTestRule.waitUntilDoesNotExist(interactionContext.filter.matcher, timeoutMillis)
    }
}

private val ComposeInteractionContext.composeTestRule get() = provider as ComposeTestRule
