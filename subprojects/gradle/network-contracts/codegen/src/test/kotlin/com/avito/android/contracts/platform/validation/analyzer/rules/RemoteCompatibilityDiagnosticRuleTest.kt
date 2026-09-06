package com.avito.android.contracts.scheme.validation.analyzer.rules

import com.avito.android.clickstream.EventsTracker
import com.avito.android.contracts.platform.analytics.ActionType
import com.avito.android.contracts.platform.analytics.NetworkContractsActionDurationEvent
import com.avito.android.contracts.platform.internal.analytics.NetworkContractsAnalyticsService
import com.avito.android.contracts.platform.scheme.validation.analyzer.rules.RemoteCompatibilityDiagnosticRule
import com.avito.android.contracts.platform.scheme.validation.data.RemoteValidationError
import com.avito.android.contracts.platform.scheme.validation.data.ValidationApiSchemesService
import com.google.common.truth.Truth.assertThat
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
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class RemoteCompatibilityDiagnosticRuleTest {

    private val validationService: ValidationApiSchemesService = mock()

    private val analyticsTracker: EventsTracker = mock()

    private val analyticsTrackerService: NetworkContractsAnalyticsService = mock {
        on { tracker } doReturn analyticsTracker
    }

    @Test
    fun `validation service does not return errors - no reports`(@TempDir schemesDir: File) = runTest {
        val schemes = listOf(schemesDir.createSchema(SCHEMA_METADATA))
        val rule = RemoteCompatibilityRuleImpl(
            schemes = schemes,
            validationService = validationService,
            analyticsTrackerService = analyticsTrackerService,
            branchName = "develop",
            modulePath = ":test",
            kind = "test",
            variantName = "test",
        )
        whenever(validationService.validate(any(), any())).thenReturn(emptyList())

        rule.analyze()

        assertThat(rule.findings).isEmpty()

        val eventCaptured = argumentCaptor<NetworkContractsActionDurationEvent> {
            verify(analyticsTracker).trackEvent(capture())
        }

        assertThat(eventCaptured.firstValue.actionType).isEqualTo(ActionType.VALIDATION)
        assertThat(eventCaptured.firstValue.modulePath).isEqualTo(":test")
    }

    @Test
    fun `validation service returns errors - reports compatibility errors`(@TempDir schemesDir: File) = runTest {
        val schemes = listOf(schemesDir.createSchema(SCHEMA_METADATA))
        val rule =
            RemoteCompatibilityRuleImpl(
                schemes = schemes,
                validationService = validationService,
                analyticsTrackerService = analyticsTrackerService,
                branchName = "develop",
                modulePath = ":test",
                kind = "test",
                variantName = "test",
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

        val eventCaptured = argumentCaptor<NetworkContractsActionDurationEvent> {
            verify(analyticsTracker).trackEvent(capture())
        }

        assertThat(eventCaptured.firstValue.actionType).isEqualTo(ActionType.VALIDATION)
        assertThat(eventCaptured.firstValue.modulePath).isEqualTo(":test")
    }

    @Test
    fun `validation service throw exception - reports error`(@TempDir schemesDir: File) = runTest {
        val schemes = listOf(schemesDir.createSchema(SCHEMA_METADATA))
        val rule =
            RemoteCompatibilityRuleImpl(
                schemes = schemes,
                validationService = validationService,
                analyticsTrackerService = analyticsTrackerService,
                branchName = "develop",
                modulePath = ":test",
                kind = "test",
                variantName = "test",
            )

        whenever(validationService.validate(any(), any())).thenThrow(RuntimeException("from test"))

        rule.analyze()

        assertThat(rule.findings).hasSize(1)
        assertThat(rule.findings[0].issue.key).isEqualTo(RemoteCompatibilityDiagnosticRule::class.java.name)
        assertThat(rule.findings[0].message).contains("from test")

        val eventCaptured = argumentCaptor<NetworkContractsActionDurationEvent> {
            verify(analyticsTracker).trackEvent(capture())
        }

        assertThat(eventCaptured.firstValue.actionType).isEqualTo(ActionType.VALIDATION)
        assertThat(eventCaptured.firstValue.modulePath).isEqualTo(":test")
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

private class RemoteCompatibilityRuleImpl(
    override val schemes: ConfigurableFileCollection,
    override val validationService: Property<ValidationApiSchemesService>,
    override val branchName: Property<String>,
    override val modulePath: Property<String>,
    override val kind: Property<String>,
    override val variantName: Property<String>,
    override val analyticsTrackerService: Property<NetworkContractsAnalyticsService>
) : RemoteCompatibilityDiagnosticRule() {

    constructor(
        schemes: List<File>,
        validationService: ValidationApiSchemesService,
        analyticsTrackerService: NetworkContractsAnalyticsService,
        branchName: String,
        modulePath: String,
        kind: String,
        variantName: String,
        objects: ObjectFactory = ProjectBuilder.builder().build().objects
    ) : this(
        schemes = objects.fileCollection().apply { setFrom(schemes) },
        validationService = objects.property<ValidationApiSchemesService>().apply { set(validationService) },
        branchName = objects.property<String>().apply { set(branchName) },
        modulePath = objects.property<String>().apply { set(modulePath) },
        kind = objects.property<String>().apply { set(kind) },
        variantName = objects.property<String>().apply { set(variantName) },
        analyticsTrackerService = objects
            .property<NetworkContractsAnalyticsService>()
            .apply { set(analyticsTrackerService) },
    )

    override fun getName(): String {
        return "remote_compatibility_test"
    }
}
