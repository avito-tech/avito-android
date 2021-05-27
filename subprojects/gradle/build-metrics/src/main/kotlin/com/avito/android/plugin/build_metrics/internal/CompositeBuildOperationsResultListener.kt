package com.avito.android.plugin.build_metrics.internal

internal class CompositeBuildOperationsResultListener(
    private val listeners: List<BuildOperationsResultListener>
) : BuildOperationsResultListener {

    override fun onBuildFinished(result: BuildOperationsResult) {
        listeners.forEach { it.onBuildFinished(result) }
    }
}
