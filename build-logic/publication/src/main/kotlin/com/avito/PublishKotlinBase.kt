package com.avito

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension

class PublishKotlinBase : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            plugins.withId("kotlin") {
                extensions.getByType(JavaPluginExtension::class.java).apply {
                    withSourcesJar()
                    withJavadocJar()
                }
            }
        }
    }
}
