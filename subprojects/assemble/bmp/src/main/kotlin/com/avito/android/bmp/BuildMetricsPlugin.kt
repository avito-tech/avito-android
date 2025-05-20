package com.avito.android.bmp

import com.avito.android.bmp.build_work.BuildWorkResultFlowAction
import com.avito.android.bmp.collector.BuildMetricsCollector
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.flow.FlowProviders
import org.gradle.api.flow.FlowScope
import org.gradle.internal.build.event.BuildEventListenerRegistryInternal
import javax.inject.Inject

@Suppress("UnstableApiUsage")
public abstract class BuildMetricsPlugin : Plugin<Project> {

    @get:Inject
    internal abstract val buildEventListenerRegistryInternal: BuildEventListenerRegistryInternal

    @get:Inject
    internal abstract val flowScope: FlowScope

    @get:Inject
    internal abstract val flowProviders: FlowProviders

    override fun apply(project: Project) {
        project.logger.lifecycle("Applied bmp")
        val collector = project
            .gradle
            .sharedServices
            .registerIfAbsent("bmp", BuildMetricsCollector::class.java)

        flowScope.always(BuildWorkResultFlowAction::class.java) {
            it.parameters.buildWorkResult.set(flowProviders.buildWorkResult)
        }
        buildEventListenerRegistryInternal.onOperationCompletion(collector)
    }
}
