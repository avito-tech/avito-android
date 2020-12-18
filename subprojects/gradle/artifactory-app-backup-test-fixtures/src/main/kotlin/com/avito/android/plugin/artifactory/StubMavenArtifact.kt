package com.avito.android.plugin.artifactory

import org.gradle.api.publish.maven.MavenArtifact
import org.gradle.api.tasks.TaskDependency
import java.io.File

class StubMavenArtifact(
    private val file: File,
    private val classifier: String
) : MavenArtifact {

    override fun getExtension() = file.extension

    override fun getFile() = file

    override fun getClassifier() = classifier

    override fun setExtension(extension: String?) {
        throw UnsupportedOperationException()
    }

    override fun setClassifier(classifier: String?) {
        throw UnsupportedOperationException()
    }

    override fun getBuildDependencies(): TaskDependency {
        throw UnsupportedOperationException()
    }

    override fun builtBy(vararg tasks: Any?) {
        throw UnsupportedOperationException()
    }
}
