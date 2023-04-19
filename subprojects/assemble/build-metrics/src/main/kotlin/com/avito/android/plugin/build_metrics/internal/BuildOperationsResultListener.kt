package com.avito.android.plugin.build_metrics.internal

internal interface BuildOperationsResultListener {
    val name: String
    fun onBuildFinished(result: BuildOperationsResult)
}
