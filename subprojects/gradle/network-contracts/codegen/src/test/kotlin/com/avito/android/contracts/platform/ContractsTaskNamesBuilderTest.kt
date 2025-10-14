package com.avito.android.contracts.platform

import com.avito.android.contracts.platform.scheme.codegen.CodegenTask
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

internal class ContractsTaskNamesBuilderTest {

    @Test
    fun `codegenTask - creates correct task name`() {
        val taskName = ContractsTaskNamesBuilder.codegenTask(
            subVariant = "debug"
        )

        assertThat(taskName).isEqualTo("${CodegenTask.NAME}Debug")
    }

    @Test
    fun `codegenTask - without subVariant`() {
        val taskName = ContractsTaskNamesBuilder.codegenTask()

        assertThat(taskName).isEqualTo(CodegenTask.NAME)
    }

    @Test
    fun `validationTask - creates correct task name`() {
        val taskName = ContractsTaskNamesBuilder.validationTask(
            kind = "test",
            subVariant = "release"
        )

        assertThat(taskName).isEqualTo("validateTestContractsRelease")
    }

    @Test
    fun `validationTask - without subVariant`() {
        val taskName = ContractsTaskNamesBuilder.validationTask(
            kind = "test"
        )

        assertThat(taskName).isEqualTo("validateTestContracts")
    }

    @Test
    fun `collectSchemesTask - creates correct task name`() {
        val taskName = ContractsTaskNamesBuilder.collectSchemesTask(
            kind = "api",
            subVariant = "staging"
        )

        assertThat(taskName).isEqualTo("collectApiSchemesStaging")
    }

    @Test
    fun `collectSchemesTask - without subVariant`() {
        val taskName = ContractsTaskNamesBuilder.collectSchemesTask(
            kind = "api"
        )

        assertThat(taskName).isEqualTo("collectApiSchemes")
    }

    @Test
    fun `updateSchemesTask - creates correct task name`() {
        val taskName = ContractsTaskNamesBuilder.updateSchemesTask(
            kind = "ui",
            subVariant = "production"
        )

        assertThat(taskName).isEqualTo("updateUiSchemesProduction")
    }

    @Test
    fun `updateSchemesTask - without subVariant`() {
        val taskName = ContractsTaskNamesBuilder.updateSchemesTask(
            kind = "ui"
        )

        assertThat(taskName).isEqualTo("updateUiSchemes")
    }

    @Test
    fun `contractsTask - creates correct task name with kebab-case kind`() {
        val taskName = ContractsTaskNamesBuilder.contractsTask(
            taskName = "custom",
            kind = "test-kind",
            variant = "debug"
        )

        assertThat(taskName).isEqualTo("customTestKindContractsDebug")
    }

    @Test
    fun `schemesTask - creates correct task name with snake_case kind`() {
        val taskName = ContractsTaskNamesBuilder.schemesTask(
            taskName = "process",
            kind = "test_kind",
            variant = "release"
        )
        assertThat(taskName).isEqualTo("processTestKindSchemesRelease")
    }
}
