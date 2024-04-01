package com.avito.android.test.compose.waiting

import androidx.compose.ui.test.junit4.ComposeTestRule
import com.avito.android.test.compose.ComposeInteractionContext

public interface ComposeWaiting {
    public val interactionContext: ComposeInteractionContext

    public fun waitUntilNodeCount(
        count: Int,
        timeoutMillis: Long = 1_000L
    ) {
        interactionContext.waitUntil(timeoutMillis) { composeTestRule ->
            composeTestRule.onAllNodes(interactionContext.filter.matcher).fetchSemanticsNodes().size == count
        }
    }

    public fun waitUntilExactlyOneExists(timeoutMillis: Long = 1_000L) {
        waitUntilNodeCount(1, timeoutMillis)
    }

    public fun waitUntilAtLeastOneExists(timeoutMillis: Long = 1_000L) {
        interactionContext.waitUntil(timeoutMillis) { composeTestRule ->
            composeTestRule.onAllNodes(interactionContext.filter.matcher).fetchSemanticsNodes().isNotEmpty()
        }
    }

    public fun waitUntilDoesNotExist(timeoutMillis: Long = 1_000L) {
        waitUntilNodeCount(0, timeoutMillis)
    }
}

private fun ComposeInteractionContext.waitUntil(
    timeoutMillis: Long = 1_000,
    condition: (ComposeTestRule) -> Boolean
) {
    val composeTestRule = provider as ComposeTestRule
    composeTestRule.waitUntil(timeoutMillis) { condition(composeTestRule) }
}
