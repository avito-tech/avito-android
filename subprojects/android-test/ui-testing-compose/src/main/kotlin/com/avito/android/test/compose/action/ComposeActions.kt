@file:Suppress("unused")

package com.avito.android.test.compose.action

import androidx.compose.ui.semantics.AccessibilityAction
import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.MouseInjectionScope
import androidx.compose.ui.test.MultiModalInjectionScope
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.TouchInjectionScope
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performMouseInput
import androidx.compose.ui.test.performMultiModalInput
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performScrollToKey
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.printToLog
import com.avito.android.test.compose.ComposeInteractionContext

public interface ComposeActions {

    public val interactionContext: ComposeInteractionContext

    public fun performClick() {
        interactionContext.perform(
            ComposeAction(
                actionName = "Click"
            ) { this.performClick() }
        )
    }

    public fun performScrollTo() {
        interactionContext.perform(
            ComposeAction(
                actionName = "ScrollTo"
            ) { this.performScrollTo() }
        )
    }

    public fun performScrollToIndex(index: Int) {
        interactionContext.perform(
            ComposeAction(
                actionName = "ScrollToIndex"
            ) { this.performScrollToIndex(index) }
        )
    }

    public fun performScrollToKey(key: Any) {
        interactionContext.perform(
            ComposeAction(
                actionName = "ScrollToKey"
            ) { this.performScrollToKey(key) }
        )
    }

    public fun performScrollToNode(
        matcher: SemanticsMatcher
    ) {
        interactionContext.perform(
            ComposeAction(
                actionName = "ScrollToNode"
            ) { this.performScrollToNode(matcher) }
        )
    }

    public fun performTouchInput(
        block: TouchInjectionScope.() -> Unit
    ) {
        interactionContext.perform(
            ComposeAction(
                actionName = "TouchInput"
            ) { this.performTouchInput(block) }
        )
    }

    @ExperimentalTestApi
    public fun performMouseInput(
        block: MouseInjectionScope.() -> Unit
    ) {
        interactionContext.perform(
            ComposeAction(
                actionName = "MouseInput"
            ) { this.performMouseInput(block) }
        )
    }

    public fun performMultiModalInput(
        block: MultiModalInjectionScope.() -> Unit
    ) {
        interactionContext.perform(
            ComposeAction(
                actionName = "MultiModalInput"
            ) { this.performMultiModalInput(block) }
        )
    }

    public fun <T : Function<Boolean>> performSemanticsAction(
        key: SemanticsPropertyKey<AccessibilityAction<T>>,
        invocation: (T) -> Unit
    ) {
        interactionContext.perform(
            ComposeAction(
                actionName = "SemanticsAction"
            ) { this.performSemanticsAction(key, invocation) }
        )
    }

    public fun performSemanticsAction(
        key: SemanticsPropertyKey<AccessibilityAction<() -> Boolean>>
    ) {
        interactionContext.perform(
            ComposeAction(
                actionName = "SemanticsAction"
            ) { this.performSemanticsAction(key) }
        )
    }

    public fun printToLog(tag: String, maxDepth: Int = Int.MAX_VALUE) {
        interactionContext.onNode().printToLog(tag, maxDepth)
    }
}

internal class ComposeActionsImpl(
    override val interactionContext: ComposeInteractionContext
) : ComposeActions
