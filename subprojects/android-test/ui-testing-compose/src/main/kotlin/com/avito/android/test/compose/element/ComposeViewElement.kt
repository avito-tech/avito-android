package com.avito.android.test.compose.element

import com.avito.android.test.compose.ComposeInteractionContext
import com.avito.android.test.compose.action.ComposeActions
import com.avito.android.test.compose.action.ComposeActionsImpl
import com.avito.android.test.compose.assertion.ComposeChecks
import com.avito.android.test.compose.assertion.ComposeChecksImpl
import com.avito.android.test.compose.waiting.ComposeWaiting
import com.avito.android.test.page_object.PageObject

/**
 * Main PageObject for Compose UI components.
 * Provides all available component actions and checks by default.
 */
public open class ComposeViewElement(
    override val interactionContext: ComposeInteractionContext
) : PageObject(), ComposeElement, ComposeActions, ComposeWaiting {
    public open val actions: ComposeActions by lazy { ComposeActionsImpl(interactionContext) }
    public open val checks: ComposeChecks by lazy { ComposeChecksImpl(interactionContext) }
}
