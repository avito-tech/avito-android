package com.avito.logger.formatter

import com.avito.logger.LoggerMetadata
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

internal class AppendMetadataFormatterTest {

    @Test
    fun `formatter - appends tag`() {
        val metadata = LoggerMetadata("Tag")
        val result = format(metadata)
        assertThat(result).isEqualTo("[Tag] some message")
    }

    @Test
    fun `formatter - appends empty tag`() {
        val metadata = LoggerMetadata("")
        val formatter = AppendMetadataFormatter(metadata)
        val result = formatter.format("some message")
        assertThat(result).isEqualTo("[] some message")
    }

    @Test
    fun `formatter - appends plugin name`() {
        val metadata = LoggerMetadata("Tag", pluginName = "MyPlugin")
        val result = format(metadata)
        assertThat(result).isEqualTo("[Tag|MyPlugin] some message")
    }

    @Test
    fun `formatter - appends plugin name and project path`() {
        val metadata = LoggerMetadata("Tag", pluginName = "MyPlugin", projectPath = ":app")
        val result = format(metadata)
        assertThat(result).isEqualTo("[Tag|MyPlugin@:app] some message")
    }

    @Test
    fun `formatter - appends plugin name task name and project path`() {
        val metadata = LoggerMetadata("Tag", pluginName = "MyPlugin", projectPath = ":app", taskName = "check")
        val result = format(metadata)
        assertThat(result).isEqualTo("[Tag|MyPlugin@:app:check] some message")
    }

    @Test
    fun `formatter - appends tag and project path`() {
        val metadata = LoggerMetadata("Tag", projectPath = ":app")
        val result = format(metadata)
        assertThat(result).isEqualTo("[Tag@:app] some message")
    }

    private fun format(metadata: LoggerMetadata): String {
        val formatter = AppendMetadataFormatter(metadata)
        return formatter.format("some message")
    }
}
