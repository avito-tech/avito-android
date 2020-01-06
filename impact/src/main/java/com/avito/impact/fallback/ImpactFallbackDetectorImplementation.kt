package com.avito.impact.fallback

import com.avito.git.GitState
import com.avito.git.gitState
import com.avito.impact.changes.ChangesDetector
import com.avito.impact.changes.newChangesDetector
import com.avito.impact.plugin.ImpactAnalysisExtension
import com.avito.kotlin.dsl.isRoot
import com.avito.utils.logging.CILogger
import com.avito.utils.logging.ciLogger
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.findByType

class ImpactFallbackDetectorImplementation(
    private val configuration: ImpactAnalysisExtension,
    private val project: Project,
    private val gitState: Provider<GitState>,
    private val logger: CILogger = project.ciLogger
) : ImpactFallbackDetector {

    override val isFallback: ImpactFallbackDetector.Result by lazy {

        val isAnalysisNeededResult = isAnalysisNeeded(
            config = configuration,
            gitState = gitState
        )

        val unsupportedChangesFound = hasUnsupportedChanges(
            project = project,
            changesDetector = newChangesDetector(
                rootDir = project.rootDir,
                targetCommit = gitState.orNull?.targetBranch?.commit,
                logger = logger
            )
        )

        if (isAnalysisNeededResult is IsAnalysisNeededResult.Skip) {
            logger.info("Impact analysis skipped. Reason: ${isAnalysisNeededResult.reason}")
        }

        return@lazy when {
            unsupportedChangesFound -> ImpactFallbackDetector.Result.Skip(reason = "Unsupported changes found")
            isAnalysisNeededResult is IsAnalysisNeededResult.Run -> ImpactFallbackDetector.Result.Run
            isAnalysisNeededResult is IsAnalysisNeededResult.Skip -> ImpactFallbackDetector.Result.Skip(reason = isAnalysisNeededResult.reason)
            else -> throw RuntimeException("Failed to detect fallback mode for impact analysis")
        }
    }

    private fun hasUnsupportedChanges(
        project: Project,
        changesDetector: ChangesDetector
    ): Boolean {
        val excludedDirectories = project.subprojects.map { it.projectDir }

        return changesDetector
            .computeChanges(project.rootDir, excludedDirectories)
            .onFailure {
                logger.info("Can't findModifiedProjects changes in the root project; ${it.message}", it.cause)
            }
            .map {
                val hasChanges = it.isNotEmpty()
                if (hasChanges) {
                    logger.info(
                        "Switch to fallback mode. Unknown changes: ${it.joinToString(
                            prefix = "----\n",
                            separator = "\n",
                            postfix = "\n----"
                        )}"
                    )
                }
                return@map hasChanges
            }
            .getOrElse { true }
    }

    companion object {

        fun from(project: Project): ImpactFallbackDetector {
            require(project.isRoot())

            val configuration: ImpactAnalysisExtension = requireNotNull(project.extensions.findByType())

            return ImpactFallbackDetectorImplementation(
                configuration,
                project = project,
                gitState = project.gitState(),
                logger = project.ciLogger
            )
        }
    }
}
