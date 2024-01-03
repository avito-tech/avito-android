package com.avito

import org.gradle.api.Plugin
import org.gradle.api.Project

class KspPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        with(project) {
            plugins.apply("com.google.devtools.ksp")
        }
    }
}
