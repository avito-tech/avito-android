package com.avito.instrumentation.rerun

import com.avito.instrumentation.configuration.InstrumentationConfiguration
import com.avito.instrumentation.createStubInstance
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.isInstanceOf
import com.nhaarman.mockitokotlin2.mock
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class RunOnTargetCommitConditionTest {

    @Test
    fun `run on target commit - yes - tryToReRunOnTargetBranch`() {
        val result = RunOnTargetCommitCondition.evaluate(
            instrumentationConfiguration = InstrumentationConfiguration.Data.createStubInstance(
                tryToReRunOnTargetBranch = true
            ),
            hasBuildOnTargetPlugin = true,
            buildOnTargetTaskProvider = { mock() }
        )

        assertThat(result).isInstanceOf<RunOnTargetCommitCondition.Result.Yes>()
    }

    @Test
    fun `run on target commit - no - default config, no build-on-target plugin`() {
        val result = RunOnTargetCommitCondition.evaluate(
            instrumentationConfiguration = InstrumentationConfiguration.Data.createStubInstance(),
            hasBuildOnTargetPlugin = false,
            buildOnTargetTaskProvider = { mock() }
        )

        assertThat(result).isInstanceOf<RunOnTargetCommitCondition.Result.No>()
    }

    @Test
    fun `run on target commit - exception - tryToReRunOnTargetBranch, no build-on-target plugin`() {
        val exception = assertThrows<IllegalStateException> {
            RunOnTargetCommitCondition.evaluate(
                instrumentationConfiguration = InstrumentationConfiguration.Data.createStubInstance(
                    tryToReRunOnTargetBranch = true
                ),
                hasBuildOnTargetPlugin = false,
                buildOnTargetTaskProvider = { mock() }
            )
        }

        assertThat(exception).hasMessageThat()
            .contains("Plugin com.avito.android.build-on-target is missing, but required")
    }
}
