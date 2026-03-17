package com.avito.android.string_transform

import com.avito.android.string_transform.internal.validation.TransformPipelinesValidator
import com.google.common.truth.Truth.assertThat
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class TransformPipelinesValidatorGradleTest {

    private lateinit var project: Project

    @BeforeEach
    fun setUp() {
        project = ProjectBuilder.builder().build()
    }

    @Test
    fun `pipeline validation - accepts pipeline - when configuration is valid`() {
        val pipeline = pipeline("alpha") {
            variant("release")
            rules { rules ->
                rules.exact("source", "target")
                rules.caseExpanded("token", "value")
            }
        }

        TransformPipelinesValidator.validate(project.path, pipeline)

        assertThat(pipeline.name).isEqualTo("alpha")
        assertThat(pipeline.variant.get()).isEqualTo("release")
        assertThat(pipeline.rules.exactRuleCount).isEqualTo(1)
        assertThat(pipeline.rules.caseExpandedRuleCount).isEqualTo(1)
    }

    @Test
    fun `pipeline validation - fails with problem - when rules are missing`() {
        val error = assertThrows(RuntimeException::class.java) {
            TransformPipelinesValidator.validate(
                project.path,
                pipeline("emptyRules") {
                    variant("release")
                }
            )
        }

        assertThat(error.allMessages()).contains("String-transform pipeline 'emptyRules' does not declare any rules")
        assertThat(error.allMessages()).contains("A pipeline without rules does nothing.")
    }

    @Test
    fun `pipeline validation - fails with problem - when variant is missing`() {
        val error = assertThrows(RuntimeException::class.java) {
            TransformPipelinesValidator.validate(
                project.path,
                pipeline("missingVariant") {
                    rules { rules ->
                        rules.exact("source", "target")
                    }
                }
            )
        }

        assertThat(error.allMessages()).contains("String-transform pipeline 'missingVariant' does not declare variant")
        assertThat(error.allMessages()).contains("Plugin binds pipelines only to exact Android variant names.")
    }

    @Test
    fun `pipeline rules - propagate invalid arguments - when rule input is malformed`() {
        val error = assertThrows(IllegalArgumentException::class.java) {
            pipeline("alpha") {
                variant("release")
                rules { rules ->
                    rules.exact("", "target")
                }
            }
        }

        assertThat(error.allMessages()).contains("transformStrings rule 'from' value must not be empty")
    }

    private fun pipeline(
        name: String,
        action: TransformPipelineSpec.() -> Unit,
    ): TransformPipelineSpec {
        return project.objects.newInstance(
            TransformPipelineSpec::class.java,
            name,
            project.objects
        ).apply(action)
    }

    private fun Throwable.allMessages(): String {
        return generateSequence(this) { it.cause }
            .mapNotNull { it.message }
            .joinToString(separator = "\n")
    }
}
