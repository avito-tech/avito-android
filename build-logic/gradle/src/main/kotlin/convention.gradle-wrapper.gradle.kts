import com.avito.android.CheckWrapper
import com.avito.android.resolveDir

plugins {
    /**
     * https://docs.gradle.org/current/userguide/base_plugin.html
     * base plugin added to add wiring on check->build tasks
     */
    base
}

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

val allProjectDirsProvider: Provider<List<Directory>> = includedProjectDirs.map { it + layout.projectDirectory }

val gradleVer = "7.2"
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

val checkGradleWrappersTaskProvider = tasks.register<CheckWrapper>("checkGradleWrappers") {
    group = "Build setup"
    description = "Checks gradle-wrapper.properties consistency for all included builds"

    expectedGradleVersion.set(gradleVer)
    expectedDistributionType.set(distribution)
    projectDirs.set(allProjectDirsProvider)
}

tasks.named("check").configure {
    dependsOn(checkGradleWrappersTaskProvider)
}

fun makeProjectNameSuitableForTaskNameSlug(projectName: String): String {
    return projectName.split(Regex("[^a-zA-Z0-9]")).joinToString(separator = "") { it.capitalize() }
}
