import com.avito.android.GenerateCommonProperties

tasks.register<GenerateCommonProperties>("generateCommonProperties") {
    group = "Build setup"
    description = "Generated common gradle.properties for all included builds"

    commonPropertiesFile.set(layout.projectDirectory.file("conf/common-gradle.properties"))

    projectDirs.set(
        gradle.includedBuilds
            .map { it.projectDir }
            .map { layout.projectDirectory.dir(it.relativeTo(projectDir).path) }
            + layout.projectDirectory
    )
}
