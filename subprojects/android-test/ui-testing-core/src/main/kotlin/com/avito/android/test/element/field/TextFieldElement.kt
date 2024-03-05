package com.avito.android.test.element.field

import android.view.View
import com.avito.android.test.InteractionContext
import com.avito.android.test.SimpleInteractionContext
import com.avito.android.test.page_object.ViewElement
import org.hamcrest.Matcher

public open class TextFieldElement(
    interactionContext: InteractionContext
) : ViewElement(interactionContext),
    FieldActions {

    override val actions: FieldActions = TextFieldAction(interactionContext)
    override val checks: TextFieldChecks = TextFieldChecksImpl(interactionContext)

    // TODO: remove this constructor and use element fabric method to create an instance
    public constructor(matcher: Matcher<View>) : this(SimpleInteractionContext(matcher))

    override fun write(text: String): Unit = actions.write(text)
    override fun writeAndPressImeAction(text: String): Unit = actions.writeAndPressImeAction(text)
    override fun pressImeAction(): Unit = actions.pressImeAction()
    override fun clear(): Unit = actions.clear()
    override fun append(text: String): Unit = actions.append(text)
}
