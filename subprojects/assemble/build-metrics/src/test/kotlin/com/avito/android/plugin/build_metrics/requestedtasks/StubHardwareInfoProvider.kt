package com.avito.android.plugin.build_metrics.requestedtasks

import com.avito.android.plugin.build_metrics.internal.gradle.requestedtasks.HardwareInfo
import com.avito.android.plugin.build_metrics.internal.gradle.requestedtasks.HardwareInfoProvider

internal class StubHardwareInfoProvider(
    private val stubInfo: HardwareInfo = HardwareInfo(ramGb = 16),
) : HardwareInfoProvider() {

    override val hardwareInfo: HardwareInfo by lazy { stubInfo }
}
