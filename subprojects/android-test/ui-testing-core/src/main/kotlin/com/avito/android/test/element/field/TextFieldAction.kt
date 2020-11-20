package com.avito.android.test.element.field

import androidx.test.espresso.action.ViewActions.actionWithAssertions
import androidx.test.espresso.action.ViewActions.clearText
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.pressImeActionButton
import com.avito.android.test.InteractionContext
import com.avito.android.test.action.Actions
import com.avito.android.test.action.ActionsImpl
import com.avito.android.test.element.field.actions.TypeText
import com.avito.android.test.espresso.EspressoActions
import com.avito.android.test.espresso.action.scroll.ScrollToIfPossibleAction

interface FieldActions : Actions {

    fun write(text: String)

    // todo move to keyboard actors
    fun writeAndPressImeAction(text: String)

    fun pressImeAction()

    fun clear()

    fun append(text: String)
}

internal class TextFieldAction(
    private val interactionContext: InteractionContext
) : FieldActions,
    Actions by ActionsImpl(interactionContext) {

    override fun write(text: String) {
        interactionContext.perform(
            actionWithAssertions(
                EspressoActions.scrollIfPossible()
            ),
            clearText(),
            actionWithAssertions(
                TypeText(
                    stringToBeTyped = text
                )
            ),
            closeSoftKeyboard()
        )
    }

    override fun writeAndPressImeAction(text: String) {
        interactionContext.perform(
            actionWithAssertions(
                EspressoActions.scrollIfPossible()
            ),
            clearText(),
            actionWithAssertions(
                TypeText(
                    stringToBeTyped = text
                )
            ),
            pressImeActionButton(),
            closeSoftKeyboard()
        )
    }

    override fun clear() {
        interactionContext.perform(
            actionWithAssertions(
                EspressoActions.scrollIfPossible()
            ),
            clearText()
        )
    }

    override fun pressImeAction() {
        interactionContext.perform(
            actionWithAssertions(ScrollToIfPossibleAction()),
            pressImeActionButton()
        )
    }

    override fun append(text: String) {
        interactionContext.perform(
            actionWithAssertions(
                EspressoActions.scrollIfPossible()
            ),
            actionWithAssertions(
                TypeText(
                    stringToBeTyped = text
                )
            )
        )
    }
}
