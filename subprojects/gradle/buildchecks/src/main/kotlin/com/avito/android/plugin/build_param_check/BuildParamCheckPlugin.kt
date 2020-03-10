package com.avito.android.plugin.build_param_check

import com.avito.android.androidSdk
import com.avito.android.plugin.build_metrics.BuildMetricTracker
import com.avito.android.sentry.environmentInfo
import com.avito.android.sentry.sentry
import com.avito.android.stats.CountMetric
import com.avito.android.stats.statsd
import com.avito.kotlin.dsl.getBooleanProperty
import com.avito.kotlin.dsl.getOptionalStringProperty
import com.avito.kotlin.dsl.isRoot
import com.avito.utils.gradle.buildEnvironment
import com.avito.utils.logging.ciLogger
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.UnknownConfigurationException
import org.gradle.api.invocation.Gradle
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.register
import org.gradle.tooling.BuildException

@Suppress("unused", "RemoveCurlyBracesFromTemplate", "UnstableApiUsage")
open class BuildParamCheckPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        printBuildEnvironment(project)

        check(project.isRoot()) {
            "Plugin must be applied to the root project but was applied to ${project.path}"
        }
        val isEnabled = project.getBooleanProperty("avito.build.paramCheck.enabled", false)
        registerRequiredTasks(project, isEnabled)

        if (!isEnabled) {
            project.ciLogger.info("Build checks are skipped")
            return
        }
        check(JavaVersion.current() == JavaVersion.VERSION_1_8) {
            "Only Java 1.8 is supported for this project but was ${javaInfo()}. " +
                "Please check java home property or install appropriate JDK."
        }
        applyChecks(project)

        checkModuleHasRequiredPlugins(project)
        checkKotlinModulesDoesNotUseApiConfiguration(project)

        showErrorsIfAny(project)
    }

    private fun registerRequiredTasks(
        project: Project,
        enabled: Boolean
    ) {
        val checkAndroidSdk =
            project.tasks.register<CheckAndroidSdkVersionTask>("checkAndroidSdkVersion") {
                group = "verification"
                description =
                    "Checks sdk version in docker against local one to prevent build cache misses"

                // don't run task if it is already compared hashes and it's ok
                // task will be executed next time if either local jar or docker jar(e.g. inputs) changed
                outputs.upToDateWhen { outputs.files.singleFile.exists() }
                onlyIf { enabled }
            }
        val checkGradleDaemonTask = project.tasks.register<CheckGradleDaemonTask>("checkGradleDaemon") {
            group = "verification"
            description = "Check gradle daemon problems"
        }
        val dynamicDependenciesTask = project.tasks.register<DynamicDependenciesTask>("checkDynamicDependencies") {
            group = "verification"
            description = "Detects dynamic dependencies"
        }
        val checkBuildEnvironment = project.tasks.register("checkBuildEnvironment") {
            it.group = "verification"
            it.description = "Check typical build problems"
            it.dependsOn(checkAndroidSdk, checkGradleDaemonTask, dynamicDependenciesTask)
        }
        if (isMac()) {
            val task = project.tasks.register<OsxLocalhostResolvingTask>("checkOsxLocalhostResolving") {
                group = "verification"
                description = "Check OSX localhost resolving issue from Java (https://thoeni.io/post/macos-sierra-java/)"
            }
            checkBuildEnvironment {
                dependsOn(task)
            }
        }
    }

    private fun isMac(): Boolean {
        return System.getProperty("os.name", "").contains("mac", ignoreCase = true)
    }

    private val validationErrors = mutableListOf<String>()

    private fun checkModuleHasRequiredPlugins(project: Project) {
        project.subprojects { subproject ->
            subproject.afterEvaluate {
                subproject.plugins.withId("com.android.application") {
                    subproject.checkAppliesRequiredPlugin("kotlin-android")
                }
                subproject.plugins.withId("com.android.library") {
                    subproject.checkAppliesRequiredPlugin("kotlin-android")
                    subproject.checkAppliesRequiredPlugin("com.avito.android.module-types")
                }
                subproject.plugins.withId("kotlin") {
                    subproject.checkAppliesRequiredPlugin("com.avito.android.module-types")
                }
                subproject.plugins.withId("org.jetbrains.kotlin.jvm") {
                    subproject.checkAppliesRequiredPlugin("com.avito.android.module-types")
                }
            }
        }
    }

    private fun showErrorsIfAny(project: Project) {
        project.gradle.projectsEvaluated {
            if (validationErrors.isNotEmpty()) {
                throw BuildException(
                    "There were errors:\n" +
                        validationErrors.joinToString(separator = "\n", transform = { " - $it" }),
                    null
                )
            }
        }
    }

    /**
     * TODO: найти issue
     * Нужно проверять что при применении плагина kotlin не используется api зависимость
     * Текущее поведение очень неожиданное: ничего не падает, но модуль который ожидает что прилетит транзитивно api зависимость не компилится,
     * то есть работает как implementation
     */
    private fun checkKotlinModulesDoesNotUseApiConfiguration(project: Project) {
        project.subprojects { subproject ->
            subproject.afterEvaluate {
                subproject.plugins.withId("kotlin") {
                    try {
                        subproject.configurations.named("api") { configuration ->
                            // без afterEvaluate коллекция всегда пустая
                            val validDependencies = configuration.dependencies.filter { it.group != null }
                            lazyCheck(validDependencies.isEmpty()) {
                                val dependenciesDescription = validDependencies.joinToString {
                                    "${it.group}:${it.name}:${it.version}"
                                }
                                "$subproject uses api dependencies $dependenciesDescription in the $configuration configuration. \n" +
                                    "It's not working correctly in the kotlin plugin. \n" +
                                    "Use 'compile' instead."
                            }
                        }
                    } catch (e: UnknownConfigurationException) {
                        // нам в этой проверке не важно почему это происходит
                        // https://avito.slack.com/archives/G0G8TFB25/p1547555630125200
                    }
                }
            }
        }
    }

    private fun lazyCheck(precondition: Boolean, message: () -> String) {
        if (!precondition) {
            validationErrors += message.invoke()
        }
    }

    private fun check(precondition: Boolean, message: () -> String) {
        if (!precondition) {
            throw BuildException(message(), null)
        }
    }

    private fun Project.checkAppliesRequiredPlugin(pluginId: String) {
        lazyCheck(plugins.hasPlugin(pluginId)) {
            "You forgot to apply '$pluginId' plugin to kotlin library module $path. it is required"
        }
    }

    private fun applyChecks(project: Project) {
        project.afterEvaluate {
            val tracker = buildTracker(project)
            val sentry = project.sentry
            val checks = listOf(
                GradlePropertiesCheck(project)
            )
            checks.forEach { checker ->
                checker.getMismatches()
                    .onSuccess {
                        it.forEach { mismatch ->
                            project.logger.warn(
                                "${mismatch.name} differs from recommended value! " +
                                    "Recommended: ${mismatch.expected} " +
                                    "Actual: ${mismatch.actual}"
                            )
                            val safeParamName = mismatch.name.replace(".", "-")
                            tracker.track(CountMetric("configuration.mismatch.${safeParamName}"))
                        }
                    }
                    .onFailure {
                        project.logger.error("[$pluginName] can't check project", it)
                        val checkerName = checker.javaClass.simpleName
                        tracker.track(CountMetric("configuration.mismatch.failed.$checkerName"))
                        sentry.get().sendException(ParamMismatchFailure(it))
                    }
            }
        }
    }

    private fun printBuildEnvironment(project: Project) {
        val isBuildCachingEnabled = project.gradle.startParameter.isBuildCacheEnabled
        val minSdk = project.getOptionalStringProperty("minSdk")
        val kaptBuildCache: Boolean = project.getBooleanProperty("kaptBuildCache")
        val kaptMapDiagnosticLocations = project.getBooleanProperty("kaptMapDiagnosticLocations")
        val javaIncrementalCompilation = project.getBooleanProperty("javaIncrementalCompilation")

        project.ciLogger.info(
            """Config information for project: ${project.displayName}:
BuildEnvironment: ${project.buildEnvironment}
${startParametersDescription(project.gradle)}
java=${javaInfo()}
JAVA_HOME=${System.getenv("JAVA_HOME")}
ANDROID_HOME=${project.androidSdk.androidHome}
org.gradle.caching=$isBuildCachingEnabled
android.enableD8=${project.getOptionalStringProperty("android.enableD8")}
android.enableR8=${project.getOptionalStringProperty("android.enableR8")}
android.enableR8.fullMode=${project.getOptionalStringProperty("android.enableR8.fullMode")}
android.builder.sdkDownload=${project.getOptionalStringProperty("android.builder.sdkDownload")}
kotlin.version=${System.getProperty("kotlinVersion")}
kotlin.incremental=${project.getOptionalStringProperty("kotlin.incremental")}
minSdk=$minSdk
preDexLibrariesEnabled=${project.getOptionalStringProperty("preDexLibrariesEnabled")}
kaptBuildCache=$kaptBuildCache
kapt.use.worker.api=${project.getOptionalStringProperty("kapt.use.worker.api")}
kapt.incremental.apt=${project.getOptionalStringProperty("kapt.incremental.apt")}
kapt.include.compile.classpath=${project.getOptionalStringProperty("kapt.include.compile.classpath")}
kaptMapDiagnosticLocations=$kaptMapDiagnosticLocations
javaIncrementalCompilation=$javaIncrementalCompilation
------------------------"""
        )
    }

    private fun buildTracker(project: Project): BuildMetricTracker {
        return BuildMetricTracker(project.environmentInfo(), project.statsd)
    }

    private fun javaInfo() =
        "${System.getProperty("java.version")} (${System.getProperty("java.vendor")})"

    private fun startParametersDescription(gradle: Gradle): String =
        gradle.startParameter.toString().replace(',', '\n')
}

private const val pluginName = "BuildParamCheckPlugin"
