package com.avito.android

import com.avito.android.clickstream.ClickStreamEventService
import com.avito.android.module_type.ModuleTypeExtension
import com.avito.kotlin.dsl.isRoot
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register

public class DemoAppMetricsPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        if (project.isRoot()) {
            registerCountDemoAppTask(project)
        } else {
            configureCountDemoAppTask(project)
        }
    }

    private fun registerCountDemoAppTask(target: Project) {
        target.tasks.register<CountDemoAppsTask>("countDemoApps") {
            val clickStreamService = ClickStreamEventService.provideClickStreamEventService(target)
            clickStreamEventService.set(clickStreamService)
            usesService(clickStreamService)
        }
    }

    private fun configureCountDemoAppTask(target: Project) {
        val moduleTypeExtension = target.extensions.getByType<ModuleTypeExtension>()
        target.rootProject.tasks.withType(CountDemoAppsTask::class.java).configureEach {
            it.moduleTypes.add(moduleTypeExtension.type)
        }
    }
}
