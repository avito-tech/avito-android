package com.avito.android.test.compose.element

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.filter
import androidx.compose.ui.test.onChildren
import com.avito.android.test.compose.ComposeInteractionContext

/**
 * PageObject для взаимодействия с UI со скроллом.
 *
 * В тестах Compose работает с UI как с деревом состоящим из [SemanticsNode],
 * никакой информации о реальных Composable функциях не содержит, только свойства [SemanticsPropertyKey].
 * Если у [SemanticsNode] есть свойство [SemanticsActions.ScrollBy], то можно использовать этот PageObject
 * и функцию [childWith] для создания PageObject.
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
