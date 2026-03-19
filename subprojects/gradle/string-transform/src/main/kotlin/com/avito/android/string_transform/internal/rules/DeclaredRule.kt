package com.avito.android.string_transform.internal.rules

import com.avito.android.string_transform.GeneratedForm

internal sealed class DeclaredRule {

    abstract val from: String
    abstract val to: String

    internal data class Exact(
        override val from: String,
        override val to: String,
    ) : DeclaredRule()

    internal data class CaseExpanded(
        override val from: String,
        override val to: String,
        val generatedForms: List<GeneratedForm>,
    ) : DeclaredRule()
}
