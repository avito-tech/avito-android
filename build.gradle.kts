plugins {
    id("convention.gradle-properties")
    id("convention.gradle-wrapper")
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
    gradle.includedBuilds
        .filterNot { it.name == "build-logic-settings" }
        .forEach { includedBuild ->
            dependsOn(includedBuild.task(":detektAll"))
        }
}

val compileAll = tasks.register("compileAll") {
    group = taskGroup
    description = "Compiles all available modules in all variants + test/androidTest sources"

    dependsOn(gradle.includedBuilds.map { it.task(":compileAll") })
}

val assembleAll = tasks.register("assembleAll") {
    group = taskGroup
    description = "Assemble all components"

    dependsOn(gradle.includedBuilds.map { it.task(":assembleAll") })
}

val checkAll = tasks.register("checkAll") {
    group = taskGroup
    description = "Run all tests and static analysis tools"

    dependsOn(gradle.includedBuilds.map { it.task(":checkAll") })

    dependsOn(detektAllTask)
    dependsOn(tasks.named("checkGradleWrappers"))
    dependsOn(tasks.named("checkCommonProperties"))
}

tasks.register("build") {
    group = taskGroup
    description = "Build and run all tests (without publishing)"

    dependsOn(assembleAll, checkAll)
}
