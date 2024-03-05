package com.avito.android.test.page_object

import androidx.test.espresso.assertion.ViewAssertions
import com.avito.android.test.InteractionContext
import com.avito.android.test.checks.Checks
import com.avito.android.test.checks.ChecksDriver
import com.avito.android.test.checks.ChecksImpl
import com.avito.android.test.matcher.BottomSheetCollapsedMatcher
import com.avito.android.test.matcher.BottomSheetExpandedMatcher
import com.avito.android.test.matcher.BottomSheetHiddenMatcher

public open class BottomSheetElement(interactionContext: InteractionContext) : ViewElement(interactionContext) {

    override val checks: ElementChecks = ElementChecks(interactionContext)

    public inner class ElementChecks(private val drivenChecks: ChecksDriver) : Checks by ChecksImpl(drivenChecks) {

        public fun isExpanded() {
            drivenChecks.check(ViewAssertions.matches(BottomSheetExpandedMatcher()))
        }

        public fun isCollapsed() {
            drivenChecks.check(ViewAssertions.matches(BottomSheetCollapsedMatcher()))
        }

        public fun isHidden() {
            drivenChecks.check(ViewAssertions.matches(BottomSheetHiddenMatcher()))
        }
    }
}
