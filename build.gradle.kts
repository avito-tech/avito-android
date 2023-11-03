import com.avito.logger.LogLevel

plugins {
    base
    // accessing version catalog here is blocked by IDE false-positive error
    // https://youtrack.jetbrains.com/issue/KTIJ-19369
    id("com.autonomousapps.dependency-analysis") version "0.78.0"
    id("convention.dependency-updates")
    id("com.avito.android.gradle-logger")
    id("com.avito.android.build-verdict")
}

buildscript {

    /**
     *  workaround to load plugin classes once:
     *  https://youtrack.jetbrains.com/issue/KT-31643#focus=Comments-27-3510019.0-0
     */
    dependencies {
        classpath(libs.androidGradle)
        classpath(libs.kotlinGradle)

        /**
         * com.autonomousapps.dependency-analysis depends on older version of okio, and it's resolved for
         * our instrumentation-tests plugin in subprojects in runtime
         */
        classpath(libs.okio)
        // TODO Delete when update sentry in infra
        classpath(libs.jacksonCore)
    }
}

gradleLogger {
    fileHandler.set(LogLevel.INFO)
    printlnHandler(true, LogLevel.INFO)
}

val taskGroup = "Avito Android build"

tasks.register<Exec>("installGitHooks") {
    group = "Build Setup"
    description = "Install local repository git hooks"
    commandLine("git")
    args("config", "core.hooksPath", ".git_hooks")
    onlyIf { !project.hasProperty("ci") }
}

val initialTaskNames: List<String> = project.gradle.startParameter.taskNames
project.gradle.startParameter.setTaskNames(initialTaskNames + listOf("installGitHooks"))

tasks.named("check") {
    dependsOn(gradle.includedBuild("build-logic").task(":check"))
    dependsOn(gradle.includedBuild("build-logic-settings").task(":check"))
}
