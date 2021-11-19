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
public fun gradlew(
    projectDir: File,
    vararg args: String,
    dryRun: Boolean = false,
    configurationCache: Boolean = false,
    expectFailure: Boolean = false,
    environment: Map<String, String>? = null,
    useModuleClasspath: Boolean = true,
    useTestFixturesClasspath: Boolean = false
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

    return try {
        val builder = GradleRunner.create().apply {
            withTestKitDir(testKitDir())
            withProjectDir(projectDir)
            withArguments(finalArgs)
            withEnvironment(environment)
            /**
             * WARNING! it breaks classpath and causes failures in AGP's tasks
             * see. MBS-5462
             * https://github.com/gradle/gradle/issues?utf8=%E2%9C%93&q=is%3Aissue+is%3Aopen+withDebug
             */
            withDebug(false)

            if (isInvokedFromIde()) {
                forwardOutput()
            }

            if (!useModuleClasspath && useTestFixturesClasspath) {
                throw IllegalArgumentException("Using testFixtures without plugin itself is ambiguous")
            }

            if (useModuleClasspath) {
                withPluginClasspath()
            }

            if (useTestFixturesClasspath) {
                withPluginClasspath(
                    pluginClasspath + testFixturesClasspath()
                )
            }
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

/**
 * Default Gradle test kit dir is a shared temp dir for all tests using test kit, like /tmp/test-kit-dir-username
 * Tt leads to concurrent issues between multiple modules in parallel:
 *   "The file lock is held by a different Gradle process"
 * and silent lock awaiting, increasing our test times on all gradleTest tasks in parallel to ~8min(for scale)
 * probably because test kit dir also used as gradle-user-home for test
 *
 * Sharing test kit dir between different processes, using maxParallelForks>1, inside single module - is ok
 * unique test-kit-dir per test is not ok! (19min)
 * So we need unique test kit dir per module.
 * This setup improves gradleTest times to ~3min
 *
 * Keeping it in build dir, to be reusable between local builds and affected by "clean" task
 */
private fun testKitDir(): File {
    return File(System.getProperty("buildDir"), "test-kit-dir").apply { mkdir() }
}

private fun testFixturesClasspath(): List<File> {
    val buildDir = File(currentProjectDir(), "build")

    return listOf(
        File(buildDir, "classes/java/testFixtures"),
        File(buildDir, "classes/kotlin/testFixtures"),
        File(buildDir, "resources/testFixtures"),
    )
}

/**
 * Current project which runs a test
 */
private fun currentProjectDir(): File =
    File(System.getProperty("user.dir"))

private fun isInvokedFromIde(): Boolean =
    System.getProperty("isInvokedFromIde")?.toBoolean() ?: false

public fun ciRun(
    projectDir: File,
    vararg args: String,
    // TODO When we doesn't create those branches in git it leads to warnings
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

public sealed class TestResult : BuildResult {

    protected abstract val dryRun: Boolean

    public data class Success(
        val result: BuildResult,
        override val dryRun: Boolean
    ) : TestResult(), BuildResult by result

    public data class ExpectedFailure(
        val result: BuildResult,
        override val dryRun: Boolean
    ) : TestResult(), BuildResult by result

    public val allTaskPaths: List<String>
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

    public val triggeredModules: Set<String>
        get() = allTaskPaths
            .map { taskPath ->
                taskPath.substring(0 until taskPath.lastIndexOf(":"))
            }.toSet()

    public fun assertThat(): TestResultSubject = TestResultSubject.assertThat(this)
}
