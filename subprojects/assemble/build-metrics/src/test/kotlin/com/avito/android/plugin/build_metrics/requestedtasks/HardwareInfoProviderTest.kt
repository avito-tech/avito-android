package com.avito.android.plugin.build_metrics.requestedtasks

import com.avito.android.plugin.build_metrics.internal.gradle.requestedtasks.HardwareInfoProvider
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

internal class HardwareInfoProviderTest {

    @Test
    fun `rounds RAM to nearest standard size - 15 GB rounds to 16`() {
        val bytes15Gb = 15L * 1024 * 1024 * 1024
        assertThat(HardwareInfoProvider.roundToStandardRamGb(bytes15Gb)).isEqualTo(16)
    }

    @Test
    fun `rounds RAM to nearest standard size - 17 GB rounds to 16`() {
        val bytes17Gb = 17L * 1024 * 1024 * 1024
        assertThat(HardwareInfoProvider.roundToStandardRamGb(bytes17Gb)).isEqualTo(16)
    }

    @Test
    fun `rounds RAM to nearest standard size - 32 GB exact`() {
        val bytes32Gb = 32L * 1024 * 1024 * 1024
        assertThat(HardwareInfoProvider.roundToStandardRamGb(bytes32Gb)).isEqualTo(32)
    }

    @Test
    fun `rounds RAM to nearest standard size - 40 GB rounds to 48`() {
        val bytes40Gb = 40L * 1024 * 1024 * 1024
        assertThat(HardwareInfoProvider.roundToStandardRamGb(bytes40Gb)).isEqualTo(48)
    }
}
