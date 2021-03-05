package com.avito.module.dependencies

import com.avito.module.internal.dependencies.TestPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project

@Suppress("UnstableApiUsage")
public class ModuleDependenciesGraphPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        require(target.rootProject == target) {
            "must be applied to the root project"
        }
        val enableTestPlugin = target.providers
            .gradleProperty("injected.from.gradle_testkit")
            .forUseAtConfigurationTime()
            .map { it.toBoolean() }
            .getOrElse(false)
        if (enableTestPlugin) {
            target.plugins.apply(TestPlugin::class.java)
        }
        target.tasks.register("findAndroidApp", FindAndroidAppTask::class.java)
    }
}
