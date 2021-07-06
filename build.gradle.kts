plugins {
    base
}

/**
 * Tests run from IDE in subprojects module can't recognize root wrapper
 */
val subprojectsWrapper by tasks.registering(Copy::class) {
    into("$rootDir/subprojects/gradle/wrapper")
    from("$rootDir/gradle/wrapper")
}

val samplesWrapper by tasks.registering(Copy::class) {
    into("$rootDir/samples/gradle/wrapper")
    from("$rootDir/gradle/wrapper")
}

tasks.withType<Wrapper> {
    // sources unavailable with BIN until https://youtrack.jetbrains.com/issue/IDEA-231667 resolved
    distributionType = Wrapper.DistributionType.ALL
    gradleVersion = "7.1"

    finalizedBy(subprojectsWrapper, samplesWrapper)
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
    gradle.includedBuilds.forEach { includedBuild ->
        dependsOn(includedBuild.task(":detektAll"))
    }
}
