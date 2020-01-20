package com.avito.android.test.element.field

import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import com.avito.android.test.InteractionContext
import com.avito.android.test.checks.Checks
import com.avito.android.test.checks.ChecksImpl
import com.avito.android.test.checks.TextFieldHintChecks

interface TextFieldChecks : Checks, TextFieldHintChecks {
    override fun withHintText(text: String)
}

internal class TextFieldChecksImpl(
    private val interactionContext: InteractionContext
) : TextFieldChecks,
    Checks by ChecksImpl(interactionContext) {

    override fun withHintText(text: String) {
        interactionContext.check(ViewAssertions.matches(ViewMatchers.withHint(text)))
    }
}