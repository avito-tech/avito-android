package com.avito.impact.util

import com.avito.android.isAndroid
import org.gradle.api.Project
import java.io.File

/**
 * Simplified android project model
 */
class AndroidProject(project: Project) : Project by project {

    init {
        require(project.isAndroid()) { "Trying to create AndroidProject from non-android ${project.path}" }
    }

    val manifest = AndroidManifest.from(project)

    override fun toString(): String {
        return "AndroidProject(name=$displayName, pkg=${manifest.getPackage()})"
    }
}

class AndroidManifest(
    private val projectDir: File,
    private val sourceSet: String = "main"
) {
    companion object {
        fun from(project: Project): AndroidManifest = AndroidManifest(project.projectDir)
    }

    private val packageParser = AndroidManifestPackageParser

    fun getPackage(): String {
        val manifest = File("${projectDir}/src/$sourceSet/AndroidManifest.xml")

        return packageParser.parse(manifest)
            ?: error("Project $projectDir doesn't have AndroidManifest or package in it")
    }
}
