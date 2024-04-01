package com.avito.android.test.compose.action

import androidx.compose.ui.test.SemanticsNodeInteraction

public interface ComposeAction {
    public val name: String
    public val description: String?
    public fun perform(view: SemanticsNodeInteraction)
    override fun toString(): String
}

internal fun ComposeAction(
    actionName: String,
    description: String? = null,
    block: SemanticsNodeInteraction.() -> Unit
) = object : ComposeAction {
    override val name: String = actionName
    override val description: String? = description

    override fun perform(view: SemanticsNodeInteraction) {
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
