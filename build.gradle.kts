@file:Suppress("UnstableApiUsage")

import io.gitlab.arturbosch.detekt.Detekt

plugins {
    /**
     * https://docs.gradle.org/current/userguide/base_plugin.html
     * base plugin added to add wiring on check->build tasks for detekt
     */
    base
    id("io.gitlab.arturbosch.detekt")
    id("com.autonomousapps.dependency-analysis") apply false
    id("com.avito.android.libraries")
}

buildscript {
    configurations.classpath {
        resolutionStrategy {
            // com.autonomousapps.dependency-analysis depends on older version of okio, and it's resolved for
            // our instrumentation-tests plugin in subprojects in runtime
            force("com.squareup.okio:okio:2.7.0")
        }
    }
}

val detektVersion: Provider<String> = providers.systemProperty("detektVersion").forUseAtConfigurationTime()

if (gradle.startParameter.taskNames.contains("buildHealth")) {
    // Reasons to disabling by default:
    // The plugin schedules heavy LocateDependenciesTask tasks even without analysis
    apply(plugin = "com.autonomousapps.dependency-analysis")
}

dependencies {
    add("detektPlugins", libs.detektFormatting)
}

tasks.withType<Wrapper> {
    // sources unavailable with BIN until https://youtrack.jetbrains.com/issue/IDEA-231667 resolved
    distributionType = Wrapper.DistributionType.ALL
    gradleVersion = "6.8.2"
}

val detektAll = tasks.register<Detekt>("detektAll") {
    description = "Runs over whole code base without the starting overhead for each module."
    parallel = true
    setSource(files(projectDir))

    /**
     * About config:
     * yaml is a copy of https://github.com/detekt/detekt/blob/master/detekt-core/src/main/resources/default-detekt-config.yml
     * all rules are disabled by default, enabled one by one
     */
    config.setFrom(files(project.rootDir.resolve("detekt.yml")))
    buildUponDefaultConfig = false

    include("**/*.kt")
    include("**/*.kts")
    exclude("**/resources/**")
    exclude("**/build/**")
    reports {
        xml.enabled = false
        html.enabled = false
    }
}

tasks.named("check").configure {
    dependsOn(detektAll)
}
