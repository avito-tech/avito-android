@file:Suppress("unused")

package com.avito.android.test.compose.filter

import androidx.compose.ui.test.SemanticsMatcher

public class ComposeFilter(
    public val matcher: SemanticsMatcher,
    public val position: Int = 0,
    public val useUnmergedTree: Boolean = false
) {
    public companion object {
        public val ANY_FILTER: ComposeFilter = ComposeFilter(
            matcher = SemanticsMatcher("screen") { true }
        )
    }
}
