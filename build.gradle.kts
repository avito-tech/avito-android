plugins {
    base
    id("convention.gradle-properties")
    id("convention.gradle-wrapper")
}

tasks.register<Exec>("installGitHooks") {
    group = "Build Setup"
    description = "Install local repository git hooks"
    commandLine("git")
    args("config", "core.hooksPath", ".git_hooks")
    onlyIf { !project.hasProperty("ci") }
}

val initialTaskNames: List<String> = project.gradle.startParameter.taskNames
project.gradle.startParameter.setTaskNames(initialTaskNames + listOf("installGitHooks"))

tasks.register("detektAll").configure {
    gradle.includedBuilds
        .filterNot { it.name == "build-logic-settings" }
        .forEach { includedBuild ->
            dependsOn(includedBuild.task(":detektAll"))
        }
}
