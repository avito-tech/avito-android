@file:Suppress("unused")

package com.avito.android.test.compose.assertion

import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertContentDescriptionContains
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsNotFocused
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertIsSelectable
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertIsToggleable
import androidx.compose.ui.test.assertRangeInfoEquals
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.assertValueEquals
import com.avito.android.test.compose.ComposeInteractionContext

public interface ComposeChecks {

    public val interactionContext: ComposeInteractionContext

    public fun isDisplayed() {
        interactionContext.check(
            ComposeAssertion(
                assertionName = "isDisplayed"
            ) { this.assertIsDisplayed() }
        )
    }

    public fun isNotDisplayed() {
        interactionContext.check(
            ComposeAssertion(
                assertionName = "isNotDisplayed"
            ) { this.assertIsNotDisplayed() }
        )
    }

    public fun isEnabled() {
        interactionContext.check(
            ComposeAssertion(
                assertionName = "isEnabled"
            ) { this.assertIsEnabled() }
        )
    }

    public fun isNotEnabled() {
        interactionContext.check(
            ComposeAssertion(
                assertionName = "isNotEnabled"
            ) { this.assertIsNotEnabled() }
        )
    }

    public fun isOn() {
        interactionContext.check(
            ComposeAssertion(
                assertionName = "isOn"
            ) { this.assertIsOn() }
        )
    }

    public fun isOff() {
        interactionContext.check(
            ComposeAssertion(
                assertionName = "isOff"
            ) { this.assertIsOff() }
        )
    }

    public fun isSelected() {
        interactionContext.check(
            ComposeAssertion(
                assertionName = "isSelected"
            ) { this.assertIsSelected() }
        )
    }

    public fun isNotSelected() {
        interactionContext.check(
            ComposeAssertion(
                assertionName = "isNotSelected"
            ) { this.assertIsNotSelected() }
        )
    }

    public fun isToggleable() {
        interactionContext.check(
            ComposeAssertion(
                assertionName = "isToggleable"
            ) { this.assertIsToggleable() }
        )
    }

    public fun isSelectable() {
        interactionContext.check(
            ComposeAssertion(
                assertionName = "isSelectable"
            ) { this.assertIsSelectable() }
        )
    }

    public fun isFocused() {
        interactionContext.check(
            ComposeAssertion(
                assertionName = "isFocused"
            ) { this.assertIsFocused() }
        )
    }

    public fun isNotFocused() {
        interactionContext.check(
            ComposeAssertion(
                assertionName = "isNotFocused"
            ) { this.assertIsNotFocused() }
        )
    }

    public fun contentDescriptionEquals(
        vararg values: String
    ) {
        interactionContext.check(
            ComposeAssertion(
                assertionName = "contentDescriptionEquals"
            ) { this.assertContentDescriptionEquals(values = values) }
        )
    }

    public fun contentDescriptionContains(
        value: String,
        substring: Boolean = false,
        ignoreCase: Boolean = false
    ) {
        interactionContext.check(
            ComposeAssertion(
                assertionName = "contentDescriptionContains"
            ) {
                this.assertContentDescriptionContains(
                    value = value,
                    substring = substring,
                    ignoreCase = ignoreCase
                )
            })
    }

    public fun textEquals(
        vararg values: String,
        includeEditableText: Boolean = true
    ) {
        interactionContext.check(
            ComposeAssertion(
                assertionName = "textEquals"
            ) {
                this.assertTextEquals(
                    values = values,
                    includeEditableText = includeEditableText
                )
            })
    }

    public fun textContains(
        value: String,
        substring: Boolean = false,
        ignoreCase: Boolean = false
    ) {
        interactionContext.check(
            ComposeAssertion(
                assertionName = "textContains"
            ) {
                this.assertTextContains(value, substring, ignoreCase)
            })
    }

    public fun valueEquals(value: String) {
        interactionContext.check(
            ComposeAssertion(
                assertionName = "valueEquals"
            ) { this.assertValueEquals(value) }
        )
    }

    public fun rangeInfoEquals(value: ProgressBarRangeInfo) {
        interactionContext.check(
            ComposeAssertion(
                assertionName = "rangeInfoEquals"
            ) { this.assertRangeInfoEquals(value) }
        )
    }

    public fun hasClickAction() {
        interactionContext.check(
            ComposeAssertion(
                assertionName = "hasClickAction"
            ) { this.assertHasClickAction() }
        )
    }

    public fun hasNoClickAction() {
        interactionContext.check(
            ComposeAssertion(
                assertionName = "hasNoClickAction"
            ) { this.assertHasNoClickAction() }
        )
    }

    public fun assert(
        matcher: SemanticsMatcher,
        messagePrefixOnError: (() -> String)? = null
    ) {
        interactionContext.check(
            ComposeAssertion(
                assertionName = "assert"
            ) {
                this.assert(
                    matcher = matcher,
                    messagePrefixOnError = messagePrefixOnError
                )
            })
    }
}

internal class ComposeChecksImpl(
    override val interactionContext: ComposeInteractionContext
) : ComposeChecks
