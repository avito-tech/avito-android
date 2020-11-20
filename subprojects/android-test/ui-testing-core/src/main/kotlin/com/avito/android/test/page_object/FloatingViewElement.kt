package com.avito.android.test.page_object

import com.avito.android.test.InteractionContext
import com.avito.android.test.checks.Checks
import com.avito.android.test.checks.ChecksImpl
import com.avito.android.test.espresso.action.ViewGetHeightAction
import com.avito.android.test.espresso.action.ViewGetTranslationYAction
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.greaterThanOrEqualTo
import org.hamcrest.Matchers.lessThan

class FloatingViewElement(
    interactionContext: InteractionContext
) : ViewElement(interactionContext, checks = FloatingViewChecksImpl(interactionContext))

class FloatingViewChecksImpl(
    private val interactionContext: InteractionContext
) : Checks by ChecksImpl(interactionContext) {

    private val translationY
        get() = ViewGetTranslationYAction().also { interactionContext.perform(it) }.translationY

    private val height
        get() = ViewGetHeightAction().also { interactionContext.perform(it) }.height

    override fun isDisplayed() {
        assertThat(translationY, lessThan(height.toFloat()))
    }

    override fun isNotDisplayed() {
        assertThat(translationY, greaterThanOrEqualTo(height.toFloat()))
    }
}
