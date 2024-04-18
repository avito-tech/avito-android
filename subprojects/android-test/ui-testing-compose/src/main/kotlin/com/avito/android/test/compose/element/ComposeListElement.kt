package com.avito.android.test.compose.element

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.filter
import androidx.compose.ui.test.onChildren
import com.avito.android.test.compose.ComposeInteractionContext

/**
 * PageObject for interacting with UI with scrolling.
 *
 * In tests, Compose works with UI as a tree consisting of [SemanticsNode],
 * does not contain any information about real Composable functions, only the [SemanticsPropertyKey] property.
 * If [SemanticsNode] has a [SemanticsActions.ScrollBy] property, you can use this PageObject
 * and the [childWith] function to create a PageObject.
 */
public abstract class ComposeListElement(
    interactionContext: ComposeInteractionContext
) : ComposeViewElement(interactionContext) {

    /**
     * Прокручивает UI до первого элемента, который соответствует [matcher]
     * и создаёт PageObject для взаимодействия с ним.
     * Метод необходимо вызывать в момент обращения к UI элементу.
     *
     * @see com.avito.android.test.compose.element.element
     */
    public inline fun <reified T : ComposeElement> childWith(
        matcher: SemanticsMatcher,
        position: Int = 0,
        useUnmergedTree: Boolean = false
    ): T {
        performScrollToNode(matcher)

        val semanticsNode = interactionContext.onNode()
            .onChildren()
            .filter(matcher)[position]
            .fetchSemanticsNode()

        return element(
            matcher = SemanticsMatcher(
                description = "node list contains node(${semanticsNode.id})",
                matcher = { node -> node.id == semanticsNode.id }
            ),
            useUnmergedTree = useUnmergedTree
        )
    }
}
