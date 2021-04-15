package com.avito.test.gradle

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.gradle.testkit.runner.UnexpectedBuildResultException
import java.io.File

/**
 * https://docs.gradle.org/current/userguide/test_kit.html
 *
 * todo expectFailure это очень неудобно, нужно попробовать сделать api
 *   при котором фактический запуск билда будет отложен до вызова проверки
 *   это порождает проблемы в случае если gradlew используется без assert в ожидании сайд-эффектов,
 *   но кажется это меньшее из зол. gradle за что?
 */
fun gradlew(
    projectDir: File,
    vararg args: String,
    dryRun: Boolean = false,
    configurationCache: Boolean = false,
    expectFailure: Boolean = false,
    environment: Map<String, String>? = null,
    useModuleClasspath: Boolean = true
): TestResult {

    val defaultArguments = mutableListOf(
        "--stacktrace",
        "-Pinjected.from.gradle_testkit=true",
        "-Pandroid.builder.sdkDownload=false"
    )

    if (dryRun) {
        defaultArguments += "--dry-run"
    }

    if (configurationCache) {
        defaultArguments += "--configuration-cache"
    }

    val finalArgs = args.asList() + defaultArguments

    println("Running gradle test kit with args: $finalArgs")

    val isInvokedFromIde = System.getProperty("isInvokedFromIde")?.toBoolean() ?: false

    return try {
        val builder = GradleRunner.create()
            .withProjectDir(projectDir)
            .withArguments(finalArgs)
            .withEnvironment(environment)
            .apply { if (useModuleClasspath) withPluginClasspath() }
            /**
             * WARNING! it breaks classpath and causes failures in AGP's tasks
             * see. MBS-5462
             * https://github.com/gradle/gradle/issues?utf8=%E2%9C%93&q=is%3Aissue+is%3Aopen+withDebug
             */
            .withDebug(false)
            .let {
                if (isInvokedFromIde) {
                    it.forwardOutput()
                } else it
            }

        if (expectFailure) {
            TestResult.ExpectedFailure(builder.buildAndFail(), dryRun)
        } else {
            TestResult.Success(builder.build(), dryRun)
        }
    } catch (e: UnexpectedBuildResultException) {
        throw AssertionError("Unexpected build result", e)
    }
}

fun ciRun(
    projectDir: File,
    vararg args: String,
    branch: String = "develop",
    targetBranch: String = "another",
    dryRun: Boolean = false,
    expectFailure: Boolean = false,
    buildType: String = "BT"
): TestResult =
    gradlew(
        projectDir,
        "-Pci=true",
        "-PbuildNumber=100",
        "-PgitBranch=$branch",
        "-PtargetBranch=$targetBranch",
        "-PteamcityBuildId=100",
        "-PteamcityBuildType=$buildType",
        "-PteamcityUrl=xxx",
        "-PteamcityApiUser=admin",
        "-PteamcityApiPassword=xxx",
        "-PatlassianUser=xxx",
        "-PatlassianPassword=xxx",
        "-PkubernetesToken=xxx",
        "-PkubernetesCaCertData=xxx",
        "-PkubernetesUrl=xxx",
        *args,
        dryRun = dryRun,
        expectFailure = expectFailure
    )

sealed class TestResult : BuildResult {
    protected abstract val dryRun: Boolean

    data class Success(
        val result: BuildResult,
        override val dryRun: Boolean
    ) : TestResult(), BuildResult by result

    data class ExpectedFailure(
        val result: BuildResult,
        override val dryRun: Boolean
    ) : TestResult(), BuildResult by result

    val allTaskPaths: List<String>
        get() {
            return if (!dryRun) {
                TaskOutcome
                    .values()
                    .flatMap { taskPaths(it) }
            } else {
                // Когда запускаем с dry-run taskoutcome не работает
                output
                    .split("\n")
                    .filter { it.startsWith(":") }
                    .map { it.trim().replace(" SKIPPED", "") }
            }
        }

    val triggeredModules
        get() = allTaskPaths
            .map { taskPath ->
                taskPath.substring(0 until taskPath.lastIndexOf(":"))
            }.toSet()

    fun assertThat() = TestResultSubject.assertThat(this)
}
