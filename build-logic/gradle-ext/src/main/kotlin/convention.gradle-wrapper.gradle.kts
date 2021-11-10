import com.avito.android.CheckWrapper
import com.avito.android.resolveDir

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

val includedProjectDirs = provider {
    gradle.includedBuilds
        .map { it.projectDir }
        .map { project.resolveDir(it) }
}

val allProjectsWrapperProperties: Provider<List<RegularFile>> = includedProjectDirs.map { it + layout.projectDirectory }
    .map { it.map { it.file("gradle/wrapper/gradle-wrapper.properties") } }

val gradleVer = "7.3"
val distribution = Wrapper.DistributionType.BIN

/**
 * Tests run from IDE in included builds can't recognize root wrapper
 * https://youtrack.jetbrains.com/issue/IDEA-262528
 */
tasks.withType<Wrapper>().configureEach {
    distributionType = distribution
    gradleVersion = gradleVer

    finalizedBy(copyWrapperTasks)
}

tasks.register<CheckWrapper>("checkGradleWrappers") {
    group = "Build setup"
    description = "Checks gradle-wrapper.properties consistency for all included builds"

    expectedGradleVersion.set(gradleVer)
    expectedDistributionType.set(distribution)
    wrapperPropertiesFiles.set(allProjectsWrapperProperties)
}

fun makeProjectNameSuitableForTaskNameSlug(projectName: String): String {
    return projectName.split(Regex("[^a-zA-Z0-9]")).joinToString(separator = "") { it.capitalize() }
}
