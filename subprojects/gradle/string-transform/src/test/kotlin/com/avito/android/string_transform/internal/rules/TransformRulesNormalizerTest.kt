package com.avito.android.string_transform.internal.rules

import com.avito.android.string_transform.GeneratedForm
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

internal class TransformRulesNormalizerTest {

    @Test
    fun `rule analysis - expands generated forms - when case-expanded rule is normalized`() {
        val result = TransformRulesNormalizer.normalize(
            declarations = listOf(
                DeclaredRule.CaseExpanded(
                    from = "token",
                    to = "value",
                    generatedForms = listOf(
                        GeneratedForm.LOWER,
                        GeneratedForm.UPPER,
                        GeneratedForm.UPPER_FIRST,
                    ),
                )
            )
        )

        assertThat(result.rules).containsExactly(
            NormalizedRule("token", "value"),
            NormalizedRule("TOKEN", "VALUE"),
            NormalizedRule("Token", "Value"),
        ).inOrder()
    }

    @Test
    fun `rule analysis - expands only selected generated forms - when case-expanded rule declares subset`() {
        val result = TransformRulesNormalizer.normalize(
            declarations = listOf(
                DeclaredRule.CaseExpanded(
                    from = "token",
                    to = "value",
                    generatedForms = listOf(
                        GeneratedForm.UPPER_FIRST,
                        GeneratedForm.LOWER,
                    ),
                )
            )
        )

        assertThat(result.rules).containsExactly(
            NormalizedRule("Token", "Value"),
            NormalizedRule("token", "value"),
        ).inOrder()
    }

    @Test
    fun `rule analysis - keeps first identical rule - when later declarations normalize to same literal pair`() {
        val result = TransformRulesNormalizer.normalize(
            declarations = listOf(
                DeclaredRule.Exact(
                    from = "source",
                    to = "target",
                ),
                DeclaredRule.Exact(
                    from = "source",
                    to = "target",
                )
            )
        )

        assertThat(result.rules).containsExactly(
            NormalizedRule("source", "target"),
        )
    }

    @Test
    fun `rule analysis - records warning - when earlier rule cascades into later rule`() {
        val result = TransformRulesNormalizer.normalize(
            declarations = listOf(
                DeclaredRule.Exact(
                    from = "a",
                    to = "b",
                ),
                DeclaredRule.Exact(
                    from = "b",
                    to = "c",
                )
            )
        )

        assertThat(result.warnings).isNotEmpty()
        assertThat(result.warnings.joinToString("\n")).contains("cascades into later rule")
    }

    @Test
    fun `rule analysis - records warning - when cascade targets non-adjacent later rule`() {
        val result = TransformRulesNormalizer.normalize(
            declarations = listOf(
                DeclaredRule.Exact(
                    from = "a",
                    to = "b",
                ),
                DeclaredRule.Exact(
                    from = "x",
                    to = "y",
                ),
                DeclaredRule.Exact(
                    from = "b",
                    to = "c",
                )
            )
        )

        assertThat(result.warnings.joinToString("\n")).contains("cascades into later rule 'b' -> 'c'")
    }

    @Test
    fun `rule analysis - records warning - when rules overlap`() {
        val result = TransformRulesNormalizer.normalize(
            declarations = listOf(
                DeclaredRule.Exact(
                    from = "token",
                    to = "value",
                ),
                DeclaredRule.Exact(
                    from = "tok",
                    to = "prefix",
                )
            )
        )

        assertThat(result.warnings.joinToString("\n")).contains("overlap and remain order-sensitive")
    }

    @Test
    fun `rule analysis - preserves tail casing - when upper-first form is generated from mixed-case value`() {
        val result = TransformRulesNormalizer.normalize(
            declarations = listOf(
                DeclaredRule.CaseExpanded(
                    from = "myValue",
                    to = "newValue",
                    generatedForms = listOf(GeneratedForm.UPPER_FIRST),
                )
            )
        )

        assertThat(result.rules).contains(
            NormalizedRule("MyValue", "NewValue"),
        )
        assertThat(result.rules).doesNotContain(
            NormalizedRule("Myvalue", "Newvalue"),
        )
    }

    @Test
    fun `rule analysis - keeps normalized rules only once - when case-expanded rules are published`() {
        val result = TransformRulesNormalizer.normalize(
            declarations = listOf(
                DeclaredRule.Exact(
                    from = "source",
                    to = "target",
                ),
                DeclaredRule.CaseExpanded(
                    from = "token",
                    to = "value",
                    generatedForms = listOf(
                        GeneratedForm.LOWER,
                        GeneratedForm.UPPER,
                        GeneratedForm.UPPER_FIRST,
                    ),
                )
            )
        )

        assertThat(result.rules).containsExactly(
            NormalizedRule("source", "target"),
            NormalizedRule("token", "value"),
            NormalizedRule("TOKEN", "VALUE"),
            NormalizedRule("Token", "Value"),
        ).inOrder()
    }

    @Test
    fun `rule analysis - records warning - when different declaration families map same source to different targets`() {
        val result = TransformRulesNormalizer.normalize(
            declarations = listOf(
                DeclaredRule.Exact(
                    from = "Token",
                    to = "ExactValue",
                ),
                DeclaredRule.CaseExpanded(
                    from = "token",
                    to = "ExpandedValue",
                    generatedForms = listOf(
                        GeneratedForm.LOWER,
                        GeneratedForm.UPPER,
                        GeneratedForm.UPPER_FIRST,
                    ),
                )
            )
        )

        assertThat(result.warnings.joinToString("\n"))
            .contains("different declaration families intersect")
    }
}
