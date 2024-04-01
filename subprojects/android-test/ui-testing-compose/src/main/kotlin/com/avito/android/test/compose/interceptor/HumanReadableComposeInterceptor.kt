package com.avito.android.test.compose.interceptor

import androidx.compose.ui.semantics.AccessibilityAction
import androidx.compose.ui.semantics.SemanticsConfiguration
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.text.AnnotatedString
import com.avito.android.test.compose.action.ComposeAction
import com.avito.android.test.compose.assertion.ComposeAssertion

public class HumanReadableComposeActionInterceptor(
    private val consumer: (String) -> Unit
) : ComposeActionInterceptor {
    override fun intercept(action: ComposeAction, node: SemanticsNodeInteraction) {
        consumer("Perform '$action' on '${node.printToString()}'")
    }
}

public class HumanReadableComposeAssertionInterceptor(
    private val consumer: (String) -> Unit
) : ComposeAssertionInterceptor {
    override fun intercept(assertion: ComposeAssertion, node: SemanticsNodeInteraction) {
        consumer("Check '$assertion' on '${node.printToString()}'")
    }
}

private fun SemanticsNodeInteraction.printToString(): String {
    val node = fetchSemanticsNode()
    val sb = StringBuilder()
    sb.append("Node(")
    sb.append("Id=#")
    sb.append(node.id)
    if (node.config.contains(SemanticsProperties.TestTag)) {
        sb.append(", Tag='")
        sb.append(node.config[SemanticsProperties.TestTag])
        sb.append("'")
    }
    sb.appendConfigInfo(node.config)
    sb.append(")")
    return sb.toString()
}

/**
 * Копия функции из [androidx.compose.ui.test.printToLog], удалены переносы строк.
 */
@Suppress("LoopWithTooManyJumpStatements")
private fun StringBuilder.appendConfigInfo(config: SemanticsConfiguration, indent: String = ", ") {
    val actions = mutableListOf<String>()
    val units = mutableListOf<String>()
    for ((key, value) in config) {
        if (key == SemanticsProperties.TestTag) {
            continue
        }

        if (value is AccessibilityAction<*> || value is Function<*>) {
            // Avoids printing stuff like "action = 'AccessibilityAction\(label=null, action=.*\)'"
            actions.add(key.name)
            continue
        }

        if (value is Unit) {
            // Avoids printing stuff like "Disabled = 'kotlin.Unit'"
            units.add(key.name)
            continue
        }

        append(indent)
        append(key.name)
        append("='")

        if (value is AnnotatedString) {
            if (value.paragraphStyles.isEmpty() && value.spanStyles.isEmpty() && value
                    .getStringAnnotations(0, value.text.length).isEmpty()
            ) {
                append(value.text)
            } else {
                // Save space if we there is text only in the object
                append(value)
            }
        } else {
            append(value)
        }

        append("'")
    }

    if (units.isNotEmpty()) {
        append(indent)
        append("[")
        append(units.joinToString(separator = ", "))
        append("]")
    }

    if (actions.isNotEmpty()) {
        append(indent)
        append("Actions = [")
        append(actions.joinToString(separator = ", "))
        append("]")
    }

    if (config.isMergingSemanticsOfDescendants) {
        append(indent)
        append("MergeDescendants = 'true'")
    }

    if (config.isClearingSemantics) {
        append(indent)
        append("ClearAndSetSemantics = 'true'")
    }
}
