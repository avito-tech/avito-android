plugins {
    id("convention.gradle-properties")
    id("convention.gradle-wrapper")
}

val taskGroup = "Avito android build"

tasks.register<Exec>("installGitHooks") {
    group = "Build Setup"
    description = "Install local repository git hooks"
    commandLine("git")
    args("config", "core.hooksPath", ".git_hooks")
    onlyIf { !project.hasProperty("ci") }
}

val initialTaskNames: List<String> = project.gradle.startParameter.taskNames
project.gradle.startParameter.setTaskNames(initialTaskNames + listOf("installGitHooks"))

val detektAllTask = tasks.register("detektAll") {
    gradle.includedBuilds
        .filterNot { it.name == "build-logic-settings" }
        .forEach { includedBuild ->
            dependsOn(includedBuild.task(":detektAll"))
        }
}

// Register lifecycle tasks in this umbrella build.
// A user/CI usually only needs these.
val checkAll = tasks.register("checkAll") {
    group = taskGroup
    description = "Run all tests and create code coverage report"

    dependsOn(
        gradle.includedBuilds
            .filter { !it.name.startsWith("build-logic") }
            .map { it.task(":checkAll") }
    )

    dependsOn(detektAllTask)
    dependsOn(tasks.named("checkGradleWrappers"))
    dependsOn(tasks.named("checkCommonProperties"))
}

val compileAll = tasks.register("compileAll") {
    group = taskGroup
    description = "Compiles all available modules in all variants + test/androidTest sources"

    dependsOn(
        gradle.includedBuilds
            .filter { !it.name.startsWith("build-logic") }
            .map { it.task(":compileAll") }
    )
}

val assembleAll = tasks.register("assembleAll") {
    group = taskGroup
    description = "Compile all code of all components"

    dependsOn(
        gradle.includedBuilds
            .filter { !it.name.startsWith("build-logic") }
            .map { it.task(":assembleAll") }
    )
}

tasks.register("build") {
    group = taskGroup
    description = "Run all tests, build (without publishing)"
    dependsOn(assembleAll, compileAll, checkAll)
}
