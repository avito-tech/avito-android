package com.avito.impact.fallback

import com.avito.git.GitState
import com.avito.git.gitState
import com.avito.impact.changes.ChangesDetector
import com.avito.impact.changes.newChangesDetector
import com.avito.impact.platformModules
import com.avito.impact.plugin.ImpactAnalysisExtension
import com.avito.impact.supportedByImpactAnalysisProjects
import com.avito.kotlin.dsl.isRoot
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.findByType
import java.io.File

internal class ImpactFallbackDetectorImplementation(
    private val configuration: ImpactAnalysisExtension,
    private val project: Project,
    private val gitState: Provider<GitState>,
) : ImpactFallbackDetector {

    private val logger = project.logger

    override val isFallback: ImpactFallbackDetector.Result by lazy {

        val isAnalysisNeededResult = isAnalysisNeeded(
            config = configuration,
            gitState = gitState
        )

        val supportedModulesDirs = supportedByImpactAnalysisProjects(project.rootProject)
            .map { it.projectDir }

        val platformModulesDirs = platformModules(project.rootProject)
            .map { it.projectDir }

        /**
         * - Changes in supported impact analysis modules are going through full impact analysis logic
         *   (configurations etc.)
         * - Platform modules are unsupported, but excluded from fallback,
         *   because it is always a dependency for some supported module
         * - Changes in parent project's build gradle files are full fallback for now
         *   (better than false negative here, skipping real changes, but could be optimized in future)
         */
        val excludedDirectories = supportedModulesDirs + platformModulesDirs

        val unsupportedChangesFound = hasUnsupportedChanges(
            rootDir = project.rootDir,
            excludedDirectories = excludedDirectories,
            changesDetector = newChangesDetector(
                rootDir = project.rootDir,
                targetCommit = gitState.orNull?.targetBranch?.commit,
            )
        )

        if (isAnalysisNeededResult is IsAnalysisNeededResult.Skip) {
            logger.info("Impact analysis skipped. Reason: ${isAnalysisNeededResult.reason}")
        }

        return@lazy when {
            unsupportedChangesFound ->
                ImpactFallbackDetector.Result.Skip(reason = "Unsupported changes found")

            isAnalysisNeededResult is IsAnalysisNeededResult.Run ->
                ImpactFallbackDetector.Result.Run

            isAnalysisNeededResult is IsAnalysisNeededResult.Skip ->
                ImpactFallbackDetector.Result.Skip(reason = isAnalysisNeededResult.reason)

            else ->
                throw RuntimeException("Failed to detect fallback mode for impact analysis")
        }
    }

    private fun hasUnsupportedChanges(
        rootDir: File,
        excludedDirectories: List<File>,
        changesDetector: ChangesDetector
    ): Boolean {
        return changesDetector
            .computeChanges(rootDir, excludedDirectories)
            .onFailure {
                logger.warn("Can't findModifiedProjects changes in the root project; ${it.message}", it.cause)
            }
            .map {
                val hasChanges = it.isNotEmpty()
                if (hasChanges) {
                    logger.info(
                        "Switch to fallback mode. Unknown changes: ${
                            it.joinToString(
                                prefix = "----\n",
                                separator = "\n",
                                postfix = "\n----"
                            )
                        }"
                    )
                }
                return@map hasChanges
            }
            .getOrElse { true }
    }

    companion object {

        fun from(project: Project): ImpactFallbackDetector {
            require(project.isRoot()) { "Project ${project.path} must be root" }

            val configuration: ImpactAnalysisExtension = requireNotNull(project.extensions.findByType())

            return ImpactFallbackDetectorImplementation(
                configuration,
                project = project,
                gitState = project.gitState(),
            )
        }
    }
}
