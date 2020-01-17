package com.avito.android.test.page_object

import androidx.test.espresso.assertion.ViewAssertions.matches
import com.avito.android.test.InteractionContext
import com.avito.android.test.checks.Checks
import com.avito.android.test.checks.ChecksImpl
import com.avito.android.test.checks.PasswordFieldChecks
import com.avito.android.test.checks.TextFieldErrorChecks
import com.avito.android.test.checks.TextFieldHintChecks
import com.avito.android.test.matcher.TextInputLayoutErrorMatcher
import com.avito.android.test.matcher.TextInputLayoutHintMatcher
import com.avito.android.test.matcher.TextInputPasswordVisibilityMatcher
import org.hamcrest.Matchers.`is`

class TextInputElement(
    interactionContext: InteractionContext
) : ViewElement(interactionContext) {

    override val checks: TextInputChecks = TextInputChecks(interactionContext)
}

class TextInputChecks(
    private val interactionContext: InteractionContext
) : Checks by ChecksImpl(interactionContext),
    PasswordFieldChecks,
    TextFieldErrorChecks,
    TextFieldHintChecks {

    override fun withErrorText(text: String) {
        interactionContext.check(matches(TextInputLayoutErrorMatcher(`is`(text))))
    }

    override fun withHintText(text: String) {
        interactionContext.check(matches(TextInputLayoutHintMatcher(`is`(text))))
    }

    override fun isPasswordVisible() {
        interactionContext.check(matches(TextInputPasswordVisibilityMatcher(`is`(true))))
    }

    override fun isPasswordHidden() {
        interactionContext.check(matches(TextInputPasswordVisibilityMatcher(`is`(false))))
    }
}
