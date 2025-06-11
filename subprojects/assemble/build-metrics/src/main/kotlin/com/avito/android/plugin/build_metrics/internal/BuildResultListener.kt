package com.avito.android.plugin.build_metrics.internal

import com.avito.android.plugin.build_metrics.internal.result.BuildResult

internal interface BuildResultListener {

    val name: String
    fun onBuildFinished(result: BuildResult)
}
