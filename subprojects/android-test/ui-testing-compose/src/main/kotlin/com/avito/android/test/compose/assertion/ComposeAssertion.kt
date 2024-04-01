package com.avito.android.test.compose.assertion

import androidx.compose.ui.test.SemanticsNodeInteraction

public interface ComposeAssertion {
    public val name: String
    public val description: String?
    public fun check(view: SemanticsNodeInteraction)
    override fun toString(): String
}

internal fun ComposeAssertion(
    assertionName: String,
    description: String? = null,
    block: SemanticsNodeInteraction.() -> Unit
) = object : ComposeAssertion {
    override val name: String = assertionName
    override val description: String? = description

    override fun check(view: SemanticsNodeInteraction) {
        view.apply(block)
    }

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append(name)
        sb.append("(")
        if (description != null) {
            sb.append("'$description'")
        }
        sb.append(")")
        return sb.toString()
    }
}
