package com.avito.android.lint.dependency

import org.gradle.api.artifacts.result.ResolvedArtifactResult
import java.io.File

internal class ResolvedArtifactResultWrapper(
    val delegate: ResolvedArtifactResult
) : ResolvedArtifactResult by delegate {

    // TODO: find a proper API
    val files: List<File> by lazy {
        when {
            delegate.file.exists() -> listOf(delegate.file)
            !delegate.file.exists() && delegate.file.path.contains("/full_jar/") -> collectCompileLibraryClasses(
                delegate.file
            )
            else -> listOf(delegate.file)
        }
    }

    private fun collectCompileLibraryClasses(file: File): List<File> {
        val compileClasses = File(file.path.substringBefore("/full_jar/") +
            "/compile_library_classes/")
        return if (compileClasses.exists()) {
            compileClasses.walk()
                .filter { it.extension == "jar" }
                .toList()
        } else {
            emptyList()
        }
    }

    override fun getFile(): File {
        throw RuntimeException("Use files instead")
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ResolvedArtifactResultWrapper

        if (delegate.id.componentIdentifier != other.delegate.id.componentIdentifier) return false

        return true
    }

    override fun hashCode(): Int {
        return delegate.id.componentIdentifier.hashCode()
    }
}
