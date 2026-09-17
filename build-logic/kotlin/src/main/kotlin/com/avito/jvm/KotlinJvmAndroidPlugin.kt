package com.avito.jvm

import org.gradle.api.Plugin
import org.gradle.api.Project

class KotlinJvmAndroidPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.plugins.apply(KotlinJvmBasePlugin::class.java)
    }
}
