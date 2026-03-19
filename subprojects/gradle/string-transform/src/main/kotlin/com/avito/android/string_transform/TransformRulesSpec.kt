package com.avito.android.string_transform

import com.avito.android.string_transform.internal.rules.DeclaredRule
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import javax.inject.Inject

public abstract class TransformRulesSpec @Inject constructor(
    objects: ObjectFactory,
) {

    internal val declaredRules: ListProperty<DeclaredRule> =
        objects.listProperty(DeclaredRule::class.java).convention(emptyList())

    public fun exact(from: String, to: String) {
        require(from.isNotEmpty()) {
            "transformStrings rule 'from' value must not be empty"
        }
        declaredRules.add(
            DeclaredRule.Exact(
                from = from,
                to = to,
            )
        )
    }

    public fun caseExpanded(from: String, to: String, vararg forms: GeneratedForm) {
        require(from.isNotEmpty()) {
            "transformStrings rule 'from' value must not be empty"
        }
        require(forms.isNotEmpty()) {
            "transformStrings caseExpanded rule must declare at least one generated form"
        }
        declaredRules.add(
            DeclaredRule.CaseExpanded(
                from = from,
                to = to,
                generatedForms = forms.toList(),
            )
        )
    }
}
