package com.avito.android.plugin.build_metrics.internal.gradle.requestedtasks

import com.avito.logger.LoggerFactory
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.process.ExecOperations
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.util.Properties
import javax.inject.Inject

internal class BuildExecutionHistory(
    private val historyFile: File,
    loggerFactory: LoggerFactory,
) {

    private val log = loggerFactory.create("BuildExecutionHistory")

    fun readPreviousState(): BuildExecutionState? {
        if (!historyFile.exists()) {
            return null
        }
        return try {
            val properties = Properties()
            historyFile.inputStream().use { properties.load(it) }
            val commit = properties.getProperty(KEY_COMMIT) ?: return null
            BuildExecutionState(commit = commit)
        } catch (e: IOException) {
            log.warn("Failed to read build execution history from $historyFile", e)
            if (historyFile.exists() && !historyFile.delete()) {
                log.warn("Failed to delete corrupted build execution history file $historyFile")
            }
            null
        }
    }

    fun writeCurrentState(state: BuildExecutionState) {
        try {
            historyFile.parentFile.mkdirs()
            val properties = Properties()
            properties.setProperty(KEY_COMMIT, state.commit)
            historyFile.outputStream().use { properties.store(it, null) }
        } catch (e: IOException) {
            log.warn("Failed to write build execution history to $historyFile", e)
        }
    }

    companion object {
        private const val KEY_COMMIT = "commit"

        internal fun storageKey(
            originUrl: String?,
            repoName: String?,
        ): String {
            val keySeed = when {
                !originUrl.isNullOrBlank() -> originUrl.trim()
                !repoName.isNullOrBlank() -> repoName.trim()
                else -> "unknown-repo"
            }

            return keySeed
                .replace("""[\\/:@.\s]+""".toRegex(), "_")
                .replace("""[^A-Za-z0-9_-]""".toRegex(), "_")
                .trim('_')
                .take(180)
                .ifBlank { "unknown-repo" }
        }

        internal fun gitOriginUrlProvider(project: Project): Provider<String> =
            project.providers.of(GitOriginUrlValueSource::class.java) {
                it.parameters.projectDir.set(project.layout.projectDirectory)
            }
    }
}

internal abstract class GitOriginUrlValueSource : ValueSource<String, GitOriginUrlValueSource.Params> {

    @get:Inject
    internal abstract val execOperations: ExecOperations

    internal interface Params : ValueSourceParameters {
        val projectDir: DirectoryProperty
    }

    override fun obtain(): String {
        val output = ByteArrayOutputStream()
        val result = execOperations.exec {
            it.workingDir(parameters.projectDir.asFile.get())
            it.commandLine("git", "config", "--get", "remote.origin.url")
            it.isIgnoreExitValue = true
            it.standardOutput = output
        }
        if (result.exitValue != 0) return ""
        return output.toString(StandardCharsets.UTF_8.name()).trim()
    }
}

internal data class BuildExecutionState(
    val commit: String,
)
