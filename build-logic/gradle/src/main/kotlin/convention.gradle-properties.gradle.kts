import com.avito.android.CheckCommonProperties
import com.avito.android.GenerateCommonProperties

plugins {
    /**
     * https://docs.gradle.org/current/userguide/base_plugin.html
     * base plugin added to add wiring on check->build tasks
     */
    base
}

val commonPropertiesFileProvider = provider { layout.projectDirectory.file("conf/common-gradle.properties") }

val includedProjectDirs = provider {
    gradle.includedBuilds
        .map { it.projectDir }
        .map { layout.resolveDir(it) }
}

val allProjectDirsProvider: Provider<List<Directory>> = includedProjectDirs.map { it + layout.projectDirectory }

tasks.register<GenerateCommonProperties>("generateCommonProperties") {
    group = "Build setup"
    description = "Generated common gradle.properties for all included builds"

    commonPropertiesFile.set(commonPropertiesFileProvider)
    projectDirs.set(allProjectDirsProvider)
}

val checkCommonPropertiesTaskProvider = tasks.register<CheckCommonProperties>("checkCommonProperties") {
    group = "Build setup"
    description = "Checks consistency for common gradle.properties for all included builds"

    commonPropertiesFile.set(commonPropertiesFileProvider)
    projectDirs.set(allProjectDirsProvider)
}

tasks.named("check").configure {
    dependsOn(checkCommonPropertiesTaskProvider)
}

/**
 * projectDir of includedBuild available only in java.io.File form
 * Using ProjectLayout getting org.gradle.api.file.Directory from it
 */
fun ProjectLayout.resolveDir(dir: File): Directory {
    return projectDirectory.dir(dir.relativeTo(projectDir).path)
}
