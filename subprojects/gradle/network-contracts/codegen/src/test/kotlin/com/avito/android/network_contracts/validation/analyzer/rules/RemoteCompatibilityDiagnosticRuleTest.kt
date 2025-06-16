package com.avito.android.network_contracts.validation.analyzer.rules

import com.avito.android.network_contracts.validation.analyzer.rules.configurations.RemoteCompatibilityRuleConfiguration
import com.avito.android.network_contracts.validation.data.ValidationApiSchemesService
import com.avito.android.network_contracts.validation.data.model.RemoteValidationError
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.property
import org.gradle.testfixtures.ProjectBuilder
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class RemoteCompatibilityDiagnosticRuleTest {

    private val validationService: ValidationApiSchemesService = mock()

    @Test
    fun `validation service does not return errors - no reports`(@TempDir schemesDir: File) = runTest {
        val schemes = listOf(schemesDir.createSchema(SCHEMA_METADATA))
        val rule = RemoteCompatibilityDiagnosticRule(
            RemoteCompatibilityRuleConfigurationImpl(
                schemes = schemes,
                validationService = validationService,
                branchName = "develop",
                modulePath = ":test",
            )
        )

        whenever(validationService.validate(any(), any())).thenReturn(emptyList())

        rule.analyze()

        assertThat(rule.findings).isEmpty()
    }

    @Test
    fun `validation service returns errors - reports compatibility errors`(@TempDir schemesDir: File) = runTest {
        val schemes = listOf(schemesDir.createSchema(SCHEMA_METADATA))
        val rule = RemoteCompatibilityDiagnosticRule(
            RemoteCompatibilityRuleConfigurationImpl(
                schemes = schemes,
                validationService = validationService,
                branchName = "develop",
                modulePath = ":test",
            )
        )

        whenever(
            validationService.validate(
                any(),
                any()
            )
        ).thenReturn(listOf(RemoteValidationError(message = "validation error", type = "test")))

        rule.analyze()

        assertThat(rule.findings).hasSize(1)
        assertThat(rule.findings[0].issue.key).isEqualTo(RemoteCompatibilityDiagnosticRule::class.java.name)
        assertThat(rule.findings[0].message).contains("validation error")
    }

    @Test
    fun `validation service throw exception - reports error`(@TempDir schemesDir: File) = runTest {
        val schemes = listOf(schemesDir.createSchema(SCHEMA_METADATA))
        val rule = RemoteCompatibilityDiagnosticRule(
            RemoteCompatibilityRuleConfigurationImpl(
                schemes = schemes,
                validationService = validationService,
                branchName = "develop",
                modulePath = ":test",
            )
        )

        whenever(validationService.validate(any(), any())).thenThrow(RuntimeException("from test"))

        rule.analyze()

        assertThat(rule.findings).hasSize(1)
        assertThat(rule.findings[0].issue.key).isEqualTo(RemoteCompatibilityDiagnosticRule::class.java.name)
        assertThat(rule.findings[0].message).contains("from test")
    }

    companion object {

        @Language("json")
        private const val SCHEMA_METADATA = """
            {
              "projectName": "test",
              "schemes": {
                "path": "content"
              }
            }
        """
    }
}

private fun File.createSchema(content: String = ""): File {
    val file = File(this, "path_schema.yaml")
    file.createNewFile()
    if (content.isNotEmpty()) {
        file.writeText(content)
    }
    return file
}

private data class RemoteCompatibilityRuleConfigurationImpl(
    override val schemes: ConfigurableFileCollection,
    override val validationService: Property<ValidationApiSchemesService>,
    override val branchName: Property<String>,
    override val modulePath: Property<String>,
) : RemoteCompatibilityRuleConfiguration {

    constructor(
        schemes: List<File>,
        validationService: ValidationApiSchemesService,
        branchName: String,
        modulePath: String,
        objects: ObjectFactory = ProjectBuilder.builder().build().objects
    ) : this(
        schemes = objects.fileCollection().apply { setFrom(schemes) },
        validationService = objects.property<ValidationApiSchemesService>().apply { set(validationService) },
        branchName = objects.property<String>().apply { set(branchName) },
        modulePath = objects.property<String>().apply { set(modulePath) },
    )

    override fun getName(): String {
        return "remote_compatibility_test"
    }
}
