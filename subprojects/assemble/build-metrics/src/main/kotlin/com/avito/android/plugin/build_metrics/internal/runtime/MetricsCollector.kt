package com.avito.android.plugin.build_metrics.internal.runtime

import com.avito.android.Result

internal interface MetricsCollector {

    fun collect(): Result<Unit>
}
