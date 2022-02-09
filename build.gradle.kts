plugins {
    id("convention.gradle-properties")
    id("convention.gradle-wrapper")
    id("convention.lifecycle")
    // accessing version catalog here is blocked by IDE false-postive error
    // https://youtrack.jetbrains.com/issue/KTIJ-19369
    id("com.autonomousapps.dependency-analysis") version "0.78.0"
    id("convention.dependency-updates")
    id("convention.detekt")
    id("com.avito.android.gradle-logger")
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
    }
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

// Register lifecycle tasks in this umbrella build.
// A user/CI usually only needs these.

val checkAll = tasks.named("checkAll") {
    group = taskGroup
    description = "Run all tests and static analysis tools"

    dependsOn(tasks.named("detektAll"))
    dependsOn(tasks.named("checkGradleWrappers"))
    dependsOn(tasks.named("checkCommonProperties"))
}

tasks.named("build") {
    group = taskGroup
    description = "Build and run all tests (without publishing)"

    dependsOn(tasks.named("assembleAll"), checkAll)
}
