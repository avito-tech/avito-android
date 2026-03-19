package com.avito.android.string_transform.internal.rules

import com.avito.android.string_transform.GeneratedForm
import com.avito.android.string_transform.TransformPipelineSpec
import java.util.Locale

internal object TransformRulesNormalizer {

    fun normalize(pipeline: TransformPipelineSpec): NormalizedPipelineRules {
        return normalize(declarations = pipeline.rules.declaredRules.get())
    }

    internal fun normalize(declarations: List<DeclaredRule>): NormalizedPipelineRules {
        val keptRules = declarations
            .flatMap(::expand)
            .distinctBy { it.rule }

        return NormalizedPipelineRules(
            rules = keptRules.map { it.rule },
            warnings = collectWarnings(keptRules),
        )
    }

    private fun expand(declaration: DeclaredRule): List<ExpandedRule> {
        return when (declaration) {
            is DeclaredRule.Exact -> listOf(
                ExpandedRule(
                    rule = NormalizedRule(
                        from = declaration.from,
                        to = declaration.to,
                    ),
                    isCaseExpanded = false,
                )
            )

            is DeclaredRule.CaseExpanded -> declaration.generatedForms.map { form ->
                ExpandedRule(
                    rule = NormalizedRule(
                        from = declaration.from.generated(form),
                        to = declaration.to.generated(form),
                    ),
                    isCaseExpanded = true,
                )
            }
        }
    }

    private fun collectWarnings(rules: List<ExpandedRule>): List<String> {
        val warnings = linkedSetOf<String>()
        collectCascadeWarnings(rules, warnings)
        collectOverlapWarnings(rules, warnings)
        collectDifferentDeclarationKindIntersectionWarnings(rules, warnings)
        return warnings.toList()
    }

    private fun collectCascadeWarnings(
        rules: List<ExpandedRule>,
        warnings: MutableSet<String>,
    ) {
        rules.forEachIndexed { index, current ->
            rules.drop(index + 1).forEach { next ->
                if (current.cascadesInto(next)) {
                    warnings += current.cascadeWarning(next)
                }
            }
        }
    }

    private fun collectOverlapWarnings(
        rules: List<ExpandedRule>,
        warnings: MutableSet<String>,
    ) {
        rules.forEachIndexed { index, current ->
            rules.drop(index + 1).forEach { next ->
                if (current.overlapsWith(next)) {
                    warnings += current.overlapWarning(next)
                }
            }
        }
    }

    private fun collectDifferentDeclarationKindIntersectionWarnings(
        rules: List<ExpandedRule>,
        warnings: MutableSet<String>,
    ) {
        rules.forEachIndexed { index, current ->
            rules.drop(index + 1).forEach { next ->
                if (current.intersectsDifferentDeclarationKind(next)) {
                    warnings += current.differentDeclarationKindIntersectionWarning()
                }
            }
        }
    }

    private fun String.generated(form: GeneratedForm): String {
        return when (form) {
            GeneratedForm.LOWER -> lowercase(Locale.ROOT)
            GeneratedForm.UPPER -> uppercase(Locale.ROOT)
            GeneratedForm.UPPER_FIRST -> replaceFirstChar { char ->
                char.titlecase(Locale.ROOT)
            }
        }
    }

    private data class ExpandedRule(
        val rule: NormalizedRule,
        val isCaseExpanded: Boolean,
    ) {
        fun cascadesInto(other: ExpandedRule): Boolean {
            return rule.to.contains(other.rule.from)
        }

        fun overlapsWith(other: ExpandedRule): Boolean {
            return rule.from.contains(other.rule.from) || other.rule.from.contains(rule.from)
        }

        fun intersectsDifferentDeclarationKind(other: ExpandedRule): Boolean {
            return isCaseExpanded != other.isCaseExpanded &&
                rule.from == other.rule.from &&
                rule.to != other.rule.to
        }

        fun cascadeWarning(other: ExpandedRule): String = buildString {
            append("Rule '")
            append(rule.from)
            append("' -> '")
            append(rule.to)
            append("' cascades into later rule '")
            append(other.rule.from)
            append("' -> '")
            append(other.rule.to)
            append("'.")
        }

        fun overlapWarning(other: ExpandedRule): String = buildString {
            append("Rules '")
            append(rule.from)
            append("' and '")
            append(other.rule.from)
            append("' overlap and remain order-sensitive.")
        }

        fun differentDeclarationKindIntersectionWarning(): String = buildString {
            append("Rules from different declaration families intersect on '")
            append(rule.from)
            append("' with different replacements.")
        }
    }
}
