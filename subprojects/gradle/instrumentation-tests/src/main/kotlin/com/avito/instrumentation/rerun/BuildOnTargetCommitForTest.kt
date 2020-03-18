package com.avito.instrumentation.rerun

import com.avito.git.Git
import com.avito.instrumentation.InstrumentationTestsAction
import com.avito.utils.BuildFailer
import com.avito.utils.hasFileContent
import com.avito.utils.logging.CILogger
import com.avito.utils.retry
import org.gradle.util.Path
import java.io.File
import java.io.Serializable
import javax.inject.Inject

/**
 * "Встроенный" запуск gradle на отличном от рабочего коммите
 */
class BuildOnTargetCommitForTest(
    private val params: Params,
    private val logger: CILogger,
    private val buildFailer: BuildFailer,
    private val nestedGradleRunner: NestedGradleRunner
) : Runnable {

    /**
     * used in worker api call in [BuildOnTargetCommitForTestTask]
     */
    @Suppress("unused")
    @Inject
    constructor(params: Params) : this(
        params = params,
        logger = params.logger,
        buildFailer = BuildFailer.RealFailer(),
        nestedGradleRunner = ConnectorNestedGradleRunner(params.logger)
    )

    sealed class Result {
        data class OK(
            val mainApk: File,
            val testApk: File
        ) : Result()

        object ApksUnavailable : Result()
    }

    companion object {
        fun fromParams(params: InstrumentationTestsAction.Params): Result {
            return if (!params.apkOnTargetCommit.hasFileContent() || !params.testApkOnTargetCommit.hasFileContent()) {
                Result.ApksUnavailable
            } else {
                Result.OK(
                    mainApk = params.apkOnTargetCommit,
                    testApk = params.testApkOnTargetCommit
                )
            }
        }
    }

    data class Params(
        val shouldFailBuild: Boolean,
        val gitAccess: GitAccess,
        val logger: CILogger,
        val tempDir: File,
        val targetCommit: String,
        val appPath: String,
        val variant: String,
        val versionName: String,
        val versionCode: Int,
        val buildScan: Boolean
    ) : Serializable

    override fun run() {
        logger.info("Nested build started with params: $params")
        val startTime = System.currentTimeMillis()

        params.tempDir.mkdirs()

        val git: Git = Git.Impl(params.tempDir) { logger.info(it) }

        val appPath = params.appPath
        val appName = Path.path(appPath).name

        git.init()
            .flatMap { git.addRemote(params.gitAccess.url) }
            .flatMap {
                retry(
                    retriesCount = 3,
                    delaySeconds = 3,
                    attemptFailedHandler = { _, error ->
                        logger.info("failed git fetch", error)
                    }) {
                    git.fetch(commitHash = params.targetCommit, depth = 1)
                        .onFailure { throw it }
                }
            }
            .flatMap { git.resetHard("FETCH_HEAD") }
            .flatMap {
                nestedGradleRunner.run(
                    workingDirectory = params.tempDir,
                    tasks = listOf(
                        "$appPath:assemble${params.variant.capitalize()}",
                        "$appPath:assembleAndroidTest"
                    ),
                    buildScan = params.buildScan,
                    jvmArgs = "-Xmx8g",
                    workers = 4,
                    projectParams = mapOf(
                        "$appName.versionName" to params.versionName,
                        "$appName.versionCode" to "${params.versionCode}",
                        "runBuildSrcTests" to "false",
                        "ci" to "true",
                        "gitBranch" to "develop",
                        "teamcityUrl" to "xxx",
                        "avito.build.metrics.enabled" to "false",
                        "avito.build.paramCheck.enabled" to "false",
                        "buildNumber" to "1",
                        "teamcityBuildType" to "xxx",
                        "slackToken" to "xxx",
                        "atlassianUser" to "STUB",
                        "atlassianPassword" to "STUB",
                        "kubernetesToken" to "STUB",
                        "kubernetesCaCertData" to "STUB",
                        "kubernetesUrl" to "STUB"
                    )
                )
            }
            .fold(
                { logger.info(buildLogMessage("completed", startTime)) },
                { exception ->
                    val message = buildLogMessage("failed", startTime)
                    logger.critical(message, exception)
                    if (params.shouldFailBuild) {
                        buildFailer.failBuild(message, exception)
                    }
                }
            )
    }

    private fun buildLogMessage(result: String, startTimeMs: Long): String =
        "Build on target branch $result for ${params.appPath} ${params.variant} on targetCommit ${params.targetCommit} " +
            "in ${System.currentTimeMillis() - startTimeMs}ms"
}
