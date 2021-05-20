/**
 * Tests run from IDE in subprojects module can't recognize root wrapper
 */
val subprojectsWrapper by tasks.registering(Copy::class) {
    from("$rootDir/gradle/wrapper")
    into("$rootDir/subprojects/gradle/wrapper")
    into("$rootDir/samples/gradle/wrapper")
}

tasks.withType<Wrapper> {
    // sources unavailable with BIN until https://youtrack.jetbrains.com/issue/IDEA-231667 resolved
    distributionType = Wrapper.DistributionType.ALL
    gradleVersion = "6.9"

    finalizedBy(subprojectsWrapper)
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
