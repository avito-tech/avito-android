package com.avito.i18n.plugin.xml

internal sealed interface StringContent {
    data class Markup(val xml: String) : StringContent
    data class Literal(val text: String) : StringContent
}
