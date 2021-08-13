/**
 * Symlinks would be better, but need a platform independent way
 */
val copyWrapperTasks = gradle.includedBuilds.map { build ->
    val projectNameSlug = makeProjectNameSuitableForTaskNameSlug(build.name)

    tasks.register<Copy>("copy${projectNameSlug}Wrapper") {
        from("$rootDir/gradle/wrapper")
        into("${build.projectDir}/gradle/wrapper")
    }
}

/**
 * Tests run from IDE in included builds can't recognize root wrapper
 * https://youtrack.jetbrains.com/issue/IDEA-262528
 */
tasks.withType<Wrapper>().configureEach {
    finalizedBy(copyWrapperTasks)
}

fun makeProjectNameSuitableForTaskNameSlug(projectName: String): String {
    return projectName.split(Regex("[^a-zA-Z0-9]")).joinToString(separator = "") { it.capitalize() }
}
