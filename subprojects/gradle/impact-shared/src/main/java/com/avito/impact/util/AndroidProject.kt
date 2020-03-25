package com.avito.impact.util

import com.avito.android.isAndroid
import org.gradle.api.Project
import java.io.File
import java.io.File.separator

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

    override fun toString(): String {
        return "AndroidProject(name=$displayName, pkg=${debug.manifest.getPackage()})"
    }
}

class Variant(
    project: AndroidProject,
    val manifest: AndroidManifest,
    buildVariant: String
) {

    private val rJavaDir =
        "${project.buildDir}${separator}intermediates${separator}runtime_symbol_list${separator}$buildVariant${separator}"

    val resourceSymbolList: R? by lazy {
        File(rJavaDir).walk()
            .find { it.name == "R.txt" }
            ?.let { R(it) }
    }
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
        private val manifestPackagePattern = Regex(" package=\"(.+)\"")

        fun from(project: Project): AndroidManifest {
            return AndroidManifest(project.projectDir)
        }
    }

    fun getPackage(): String {
        val manifest = File("${projectDir}/src/$sourceSet/AndroidManifest.xml").readText()
        return manifestPackagePattern.find(manifest)?.groupValues?.get(1)
            ?: throw NullPointerException("Project $projectDir doesn't have AndroidManifest or package in it")
    }
}
