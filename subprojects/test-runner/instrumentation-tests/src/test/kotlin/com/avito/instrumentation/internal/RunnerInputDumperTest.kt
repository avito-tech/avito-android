package com.avito.instrumentation.internal

import com.avito.runner.config.InstrumentationConfigurationData
import com.avito.runner.config.RunnerInputParams
import com.avito.runner.config.TargetConfigurationData
import com.avito.runner.config.createStubInstance
import com.avito.runner.model.InstrumentationParameters
import com.google.common.truth.Truth.assertThat
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class RunnerInputDumperTest {

    @Test
    fun `instrumentation params with pem values - dump input - pem values are hidden`(@TempDir dumpDir: File) {
        val params = InstrumentationParameters(
            mapOf(
                "avito.tls.crt" to stubPem("CERTIFICATE"),
                "avito.tls.key" to stubPem("PRIVATE KEY"),
                "deviceName" to "api22",
            )
        )
        val input = RunnerInputParams.createStubInstance(
            instrumentationConfiguration = InstrumentationConfigurationData.createStubInstance(
                instrumentationParams = params,
                targets = listOf(TargetConfigurationData.createStubInstance(instrumentationParams = params)),
            ),
            outputDir = dumpDir,
            macrobenchmarkOutputDir = dumpDir,
        )

        RunnerInputDumper(dumpDir).dumpInput(input, isGradleTestKitRun = false)

        val configuration = JsonParser.parseString(File(dumpDir, "test-runner-args-dump.json").readText())
            .asJsonObject
            .getAsJsonObject("instrumentationConfiguration")
        val expected = mapOf(
            "avito.tls.crt" to "***",
            "avito.tls.key" to "***",
            "deviceName" to "api22",
        )
        assertThat(configuration.instrumentationParams()).containsExactlyEntriesIn(expected)
        assertThat(configuration.getAsJsonArray("targets").single().asJsonObject.instrumentationParams())
            .containsExactlyEntriesIn(expected)
    }

    private fun JsonObject.instrumentationParams(): Map<String, String> =
        getAsJsonObject("instrumentationParams").entrySet().associate { it.key to it.value.asString }
}
