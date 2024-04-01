package com.avito.android.test.compose.element

import com.avito.android.test.compose.ComposeInteractionContext
import com.avito.android.test.compose.action.ComposeTextActions
import com.avito.android.test.compose.action.ComposeTextActionsImpl

/**
 * PageObject для текстовых полей. Содержит дополнительные действия [ComposeTextActions].
 */
public open class ComposeTextElement(
    interactionContext: ComposeInteractionContext
) : ComposeViewElement(interactionContext), ComposeTextActions {
    override val actions: ComposeTextActions = ComposeTextActionsImpl(interactionContext)
}
