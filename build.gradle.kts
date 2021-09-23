plugins {
    id("convention.gradle-properties")
    id("convention.gradle-wrapper")
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
val detektAllTask = tasks.register("detektAll") {
    group = taskGroup
    description = "Run detekt in all included builds"

    dependsOn(includedBuildTasks(":detektAll"))
}

val compileAll = tasks.register("compileAll") {
    group = taskGroup
    description = "Compiles all available modules in all variants + test/androidTest sources"

    dependsOn(includedBuildTasks(":compileAll"))
}

val assembleAll = tasks.register("assembleAll") {
    group = taskGroup
    description = "Assemble all components"

    dependsOn(includedBuildTasks(":assembleAll"))
}

tasks.register("resolveAndLockAll") {
    group = taskGroup
    description = "Resolve all dependencies and write locks"

    dependsOn(includedBuildTasks(":resolveAndLockAll"))
}

val checkAll = tasks.register("checkAll") {
    group = taskGroup
    description = "Run all tests and static analysis tools"

    dependsOn(includedBuildTasks(":checkAll"))

    dependsOn(detektAllTask)
    dependsOn(tasks.named("checkGradleWrappers"))
    dependsOn(tasks.named("checkCommonProperties"))
}

tasks.register("build") {
    group = taskGroup
    description = "Build and run all tests (without publishing)"

    dependsOn(assembleAll, checkAll)
}

fun includedBuildTasks(path: String): List<TaskReference> {
    return gradle.includedBuilds
        .filter { !it.name.startsWith("build-logic") }
        .map { it.task(path) }
}
