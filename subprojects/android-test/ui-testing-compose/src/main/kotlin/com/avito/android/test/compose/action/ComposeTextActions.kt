@file:Suppress("unused")

package com.avito.android.test.compose.action

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextInputSelection
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.text.TextRange
import com.avito.android.test.compose.ComposeInteractionContext

public interface ComposeTextActions : ComposeActions {

    public fun performTextClearance() {
        interactionContext.perform(
            ComposeAction(
                actionName = "TextClearance"
            ) { this.performTextClearance() }
        )
    }

    public fun performTextInput(text: String) {
        interactionContext.perform(
            ComposeAction(
                actionName = "TextInput"
            ) { this.performTextInput(text) }
        )
    }

    @ExperimentalTestApi
    public fun performTextInputSelection(selection: TextRange) {
        interactionContext.perform(
            ComposeAction(
                actionName = "TextInputSelection"
            ) { this.performTextInputSelection(selection) }
        )
    }

    public fun performTextReplacement(text: String) {
        interactionContext.perform(
            ComposeAction(
                actionName = "TextReplacement"
            ) { this.performTextReplacement(text) }
        )
    }

    public fun performImeAction() {
        interactionContext.perform(
            ComposeAction(
                actionName = "ImeAction"
            ) { this.performImeAction() }
        )
    }
}

internal class ComposeTextActionsImpl(
    override val interactionContext: ComposeInteractionContext
) : ComposeTextActions
