package com.avito.android.test.compose.element

import androidx.compose.ui.test.SemanticsMatcher
import com.avito.android.test.compose.ComposeInteractionContext
import com.avito.android.test.compose.filter.ComposeFilter

/**
 * Базовый контракт связывающий все PageObject для Compose,
 * в рамках которого можно создавать вложенные PageObject.
 */
public interface ComposeElement {
    public val interactionContext: ComposeInteractionContext
}

/**
 * Создаёт PageObject соответсвующий условиям [matcher] и [position].
 *
 * @param position Указывает позицию в коллекции, если поиск по [matcher] выдаёт больше одного UI-элемента.
 * Значение по умолчанию соответствует первому найденому элементу.
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
