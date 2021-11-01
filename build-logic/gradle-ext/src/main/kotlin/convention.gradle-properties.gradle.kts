import com.avito.android.CheckCommonProperties
import com.avito.android.GenerateCommonProperties
import com.avito.android.resolveDir

val commonPropertiesFileProvider = provider { layout.projectDirectory.file("conf/common-gradle.properties") }

val includedProjectDirs = provider {
    gradle.includedBuilds
        .map { it.projectDir }
        .map { project.resolveDir(it) }
}

val allProjectDirsProvider: Provider<List<Directory>> = includedProjectDirs.map { it + layout.projectDirectory }

tasks.register<GenerateCommonProperties>("generateCommonProperties") {
    group = "Build setup"
    description = "Generated common gradle.properties for all included builds"

    commonPropertiesFile.set(commonPropertiesFileProvider)
    projectDirs.set(allProjectDirsProvider)
}

tasks.register<CheckCommonProperties>("checkCommonProperties") {
    group = "Build setup"
    description = "Checks consistency for common gradle.properties for all included builds"

    commonPropertiesFile.set(commonPropertiesFileProvider)
    gradlePropertiesFiles.set(allProjectDirsProvider.map { it.map { it.file("gradle.properties") } })
}
