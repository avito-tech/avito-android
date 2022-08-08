package com.avito.android.plugin.build_metrics.runtime

import com.avito.android.plugin.build_metrics.internal.runtime.MemoryInfo
import com.avito.android.plugin.build_metrics.internal.runtime.OsMetricsSender

internal class OsMetricsSenderStub : OsMetricsSender {

    val sendInvocations = mutableListOf<MemoryInfo>()

    override fun send(memoryInfo: MemoryInfo) {
        sendInvocations.add(memoryInfo)
    }
}
