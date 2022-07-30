package com.avito.android.plugin.build_metrics.runtime

import com.avito.android.plugin.build_metrics.internal.runtime.HeapInfo
import com.avito.android.plugin.build_metrics.internal.runtime.JvmMetricsSender
import com.avito.android.plugin.build_metrics.internal.runtime.LocalVm

internal class JvmMetricsSenderStub : JvmMetricsSender {

    val sendInvocations = mutableListOf<Pair<LocalVm, HeapInfo>>()

    override fun send(vm: LocalVm, heapInfo: HeapInfo) {
        sendInvocations.add(vm to heapInfo)
    }
}
