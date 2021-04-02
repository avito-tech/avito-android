import io.gitlab.arturbosch.detekt.Detekt

plugins {
    /**
     * https://docs.gradle.org/current/userguide/base_plugin.html
     * base plugin added to add wiring on check->build tasks for detekt
     */
    base
    id("io.gitlab.arturbosch.detekt")
    id("com.autonomousapps.dependency-analysis") version "0.55.0" apply false

    // workaround to load plugin classes once:
    // https://youtrack.jetbrains.com/issue/KT-31643#focus=Comments-27-3510019.0-0
    id("org.jetbrains.kotlin.jvm") apply false
    id("com.android.application") apply false
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

if (gradle.startParameter.taskNames.contains("buildHealth")) {
    // Reasons to disabling by default:
    // The plugin schedules heavy LocateDependenciesTask tasks even without analysis
    apply(plugin = "com.autonomousapps.dependency-analysis")
}

dependencies {
    add("detektPlugins", libs.detektFormatting)
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
