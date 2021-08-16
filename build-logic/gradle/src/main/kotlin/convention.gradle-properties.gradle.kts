import com.avito.android.GenerateCommonProperties

tasks.register<GenerateCommonProperties>("generateCommonProperties") {
    group = "Build setup"
    description = "Generated common gradle.properties for all included builds"

    commonPropertiesFile.set(layout.projectDirectory.file("conf/common-gradle.properties"))

    projectDirs.set(getIncludedProjectDirs() + layout.projectDirectory)
}

fun getIncludedProjectDirs(): List<Directory> {
    return gradle.includedBuilds
        .map { it.projectDir }
        .map { layout.resolveDir(it) }
}

/**
 * projectDir of includedBuild available only in java.io.File form
 * Using ProjectLayout getting org.gradle.api.file.Directory from it
 */
fun ProjectLayout.resolveDir(dir: File): Directory {
    return projectDirectory.dir(dir.relativeTo(projectDir).path)
}
