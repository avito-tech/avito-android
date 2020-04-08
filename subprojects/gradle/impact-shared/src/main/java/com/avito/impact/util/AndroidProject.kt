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

    val debug = Variant(
        this,
        AndroidManifest.from(project),
        buildVariant = "debug"
    )

    val release = Variant(
        this,
        AndroidManifest.from(project),
        buildVariant = "release"
    )

    override fun toString(): String {
        return "AndroidProject(name=$displayName, pkg=${debug.manifest.getPackage()})"
    }
}

class Variant(
    project: AndroidProject,
    val manifest: AndroidManifest,
    buildVariant: String
) {
}

class R(val file: File) {

    companion object {
        private val packagePattern = Regex("package (.+);")
    }

    private val content: String by lazy { file.readText() }

    fun getPackage(): String {
        val groupValues = packagePattern.find(content)?.groupValues
        return if (groupValues == null || groupValues.size <= 1) {
            throw NullPointerException("Can't find package in $file")
        } else {
            groupValues[1]
        }
    }

    fun contains(decimalId: Int): Boolean = content.contains("0x${Integer.toHexString(decimalId)}")
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
