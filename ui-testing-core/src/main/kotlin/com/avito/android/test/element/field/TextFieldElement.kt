package com.avito.android.test.element.field

import android.view.View
import com.avito.android.test.InteractionContext
import com.avito.android.test.SimpleInteractionContext
import com.avito.android.test.page_object.ViewElement
import org.hamcrest.Matcher

open class TextFieldElement(
    interactionContext: InteractionContext
) : ViewElement(interactionContext),
    FieldActions {

    override val actions: FieldActions = TextFieldAction(interactionContext)
    override val checks: TextFieldChecks = TextFieldChecksImpl(interactionContext)

    // TODO: remove this constructor and use element fabric method to create an instance
    constructor(matcher: Matcher<View>) : this(SimpleInteractionContext(matcher))

    override fun write(text: String) = actions.write(text)
    override fun writeAndPressImeAction(text: String) = actions.writeAndPressImeAction(text)
    override fun pressImeAction() = actions.pressImeAction()
    override fun clear() = actions.clear()
    override fun append(text: String) = actions.append(text)
}
