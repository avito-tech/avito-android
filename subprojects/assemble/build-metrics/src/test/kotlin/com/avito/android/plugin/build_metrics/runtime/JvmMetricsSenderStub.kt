package com.avito.android.plugin.build_metrics.runtime

import com.avito.android.plugin.build_metrics.internal.runtime.jvm.HeapInfo
import com.avito.android.plugin.build_metrics.internal.runtime.jvm.JvmMetricsSender
import com.avito.android.plugin.build_metrics.internal.runtime.jvm.LocalVm

internal class JvmMetricsSenderStub : JvmMetricsSender {

    val sendInvocations = mutableListOf<Pair<LocalVm, HeapInfo>>()

    override fun send(vm: LocalVm, heapInfo: HeapInfo) {
        sendInvocations.add(vm to heapInfo)
    }
}
