package com.avito.android.test.compose.element

import androidx.compose.ui.test.SemanticsMatcher
import com.avito.android.test.compose.ComposeInteractionContext
import com.avito.android.test.compose.filter.ComposeFilter

/**
 * The main contract that all PageObjects for Compose should implement.
 * Allows you to create nested PageObjects.
 */
public interface ComposeElement {
    public val interactionContext: ComposeInteractionContext
}

/**
 * Creates a PageObject using the [matcher] and [position] criteria.
 *
 * @param position Specifies the position in the collection if the [matcher] search returns more than one UI element.
 * The default value is the first item found.
 * @see androidx.compose.ui.test.SemanticsNodeInteractionsProvider.onAllNodes
 */
public inline fun <reified T : ComposeElement> ComposeElement.element(
    matcher: SemanticsMatcher,
    position: Int = 0,
    useUnmergedTree: Boolean = false
): T {
    return T::class.java.getConstructor(
        ComposeInteractionContext::class.java
    ).newInstance(
        interactionContext.provideChildContext(
            ComposeFilter(matcher, position, useUnmergedTree)
        )
    )
}
