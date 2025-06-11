package com.avito.android.plugin.build_metrics.internal.result

import com.avito.android.plugin.build_metrics.internal.BuildOperationsResultProvider
import org.gradle.api.flow.BuildWorkResult
import org.gradle.api.flow.FlowAction
import org.gradle.api.flow.FlowParameters
import org.gradle.api.provider.Property
import org.gradle.api.services.ServiceReference
import org.gradle.api.tasks.Input

@Suppress("UnstableApiUsage")
internal abstract class BuildResultFlowAction : FlowAction<BuildResultFlowAction.Params> {

    internal interface Params : FlowParameters {

        @get:ServiceReference
        val buildMetricsService: Property<BuildOperationsResultProvider>

        @get:Input
        val buildWorkResult: Property<BuildWorkResult>
    }

    override fun execute(parameters: Params) {
        parameters.buildMetricsService.get().onBuildResult(
            parameters.buildWorkResult.get()
        )
    }
}
