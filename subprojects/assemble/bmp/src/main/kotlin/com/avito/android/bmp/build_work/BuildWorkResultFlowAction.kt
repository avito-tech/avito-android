package com.avito.android.bmp.build_work

import com.avito.android.bmp.collector.BuildMetricsCollector
import org.gradle.api.flow.BuildWorkResult
import org.gradle.api.flow.FlowAction
import org.gradle.api.flow.FlowParameters
import org.gradle.api.provider.Property
import org.gradle.api.services.ServiceReference
import org.gradle.api.tasks.Input

@Suppress("UnstableApiUsage")
internal abstract class BuildWorkResultFlowAction : FlowAction<BuildWorkResultFlowAction.Params> {
    interface Params : FlowParameters {

        @get:ServiceReference
        val buildMetricsCollector: Property<BuildMetricsCollector>

        @get:Input
        val buildWorkResult: Property<BuildWorkResult>
    }

    override fun execute(parameters: Params) {
        parameters.buildMetricsCollector.get().onBuildResult(parameters.buildWorkResult.get())
    }
}
