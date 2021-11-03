package com.avito.android.plugin.build_metrics.internal

internal class LazyBuildOperationsResultListener(
    private val listener: Lazy<BuildOperationsResultListener>
) : BuildOperationsResultListener {
    override fun onBuildFinished(result: BuildOperationsResult) {
        listener.value.onBuildFinished(result)
    }
}
