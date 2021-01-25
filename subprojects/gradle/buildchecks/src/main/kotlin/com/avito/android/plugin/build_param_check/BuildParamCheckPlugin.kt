package com.avito.android.plugin.build_param_check

import com.avito.android.AndroidSdk
import com.avito.android.plugin.build_metrics.BuildMetricTracker
import com.avito.android.plugin.build_param_check.BuildChecksExtension.Check
import com.avito.android.plugin.build_param_check.incremental_check.IncrementalKaptTask
import com.avito.android.sentry.environmentInfo
import com.avito.android.sentry.sentry
import com.avito.android.stats.CountMetric
import com.avito.android.stats.statsd
import com.avito.kotlin.dsl.getBooleanProperty
import com.avito.kotlin.dsl.getOptionalStringProperty
import com.avito.kotlin.dsl.isRoot
import com.avito.logger.GradleLoggerFactory
import com.avito.logger.Logger
import com.avito.utils.gradle.buildEnvironment
import org.gradle.StartParameter
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.invocation.Gradle
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.register
import org.gradle.tooling.BuildException

@Suppress("unused")
open class BuildParamCheckPlugin : Plugin<Project> {

    private val validationErrors = mutableListOf<String>()

    @Suppress("UnstableApiUsage")
    private val Project.pluginIsEnabled: Boolean
        get() = providers
            .gradleProperty(enabledProp)
            .forUseAtConfigurationTime()
            .map { it.toBoolean() }
            .getOrElse(true)

    override fun apply(project: Project) {
        val extension = project.extensions.create<BuildChecksExtension>(extensionName)

        check(project.isRoot()) {
            "Plugin must be applied to the root project but was applied to ${project.path}"
        }
        if (!project.pluginIsEnabled) return

        val logger = GradleLoggerFactory.getLogger(this, project)

        val accessor = SystemValuesAccessor(project.providers)

        printBuildEnvironment(project, accessor, logger)

        project.afterEvaluate {
            val checks = ChecksFilter(extension).enabledChecks()
            checks
                .filterIsInstance<BuildChecksExtension.RequireParameters>()
                .forEach {
                    it.validate()
                }

            registerRequiredTasks(project, accessor, checks)

            if (checks.hasInstance<Check.JavaVersion>()) {
                checkJavaVersion(checks.getInstance(), accessor)
            }
            if (checks.hasInstance<Check.GradleProperties>()) {
                checkGradleProperties(project, accessor)
            }
            if (checks.hasInstance<Check.ModuleTypes>()) {
                checkModuleHasRequiredPlugins(project)
            }

            showErrorsIfAny(project)
        }
    }

    private fun checkJavaVersion(check: Check.JavaVersion, accessor: SystemValuesAccessor) {
        check(JavaVersion.current() == check.version) {
            "Only ${check.version} is supported for this project but was ${accessor.javaInfo}. " +
                "Please check java home property or install appropriate JDK."
        }
    }

    private fun registerRequiredTasks(project: Project, accessor: SystemValuesAccessor, checks: List<Check>) {
        val checkBuildEnvironment = project.tasks.register("checkBuildEnvironment") {
            it.group = "verification"
            it.description = "Check typical build problems"
        }
        project.gradle.startParameter.addTaskNames(":checkBuildEnvironment")

        if (checks.hasInstance<Check.AndroidSdk>()) {
            val check = checks.getInstance<Check.AndroidSdk>()
            val task = project.tasks.register<CheckAndroidSdkVersionTask>("checkAndroidSdkVersion") {
                group = "verification"
                description = "Checks sdk version in docker against local one to prevent build cache misses"

                compileSdkVersion.set(check.compileSdkVersion)
                platformRevision.set(check.revision)
                // don't run task if it is already compared hashes and it's ok
                // task will be executed next time if either local jar or docker jar(e.g. inputs) changed
                outputs.upToDateWhen { outputs.files.singleFile.exists() }
            }
            checkBuildEnvironment {
                dependsOn(task)
            }
        }
        if (checks.hasInstance<Check.GradleDaemon>()) {
            val task = project.tasks.register<CheckGradleDaemonTask>("checkGradleDaemon") {
                group = "verification"
                description = "Check gradle daemon problems"
            }
            checkBuildEnvironment {
                dependsOn(task)
            }
        }
        if (checks.hasInstance<Check.DynamicDependencies>()) {
            val task = project.tasks.register<DynamicDependenciesTask>("checkDynamicDependencies") {
                group = "verification"
                description = "Detects dynamic dependencies"
            }
            checkBuildEnvironment {
                dependsOn(task)
            }
        }
        if (checks.hasInstance<Check.UniqueRClasses>()) {
            UniqueRClassesTaskFactory(project, checks.getInstance())
                .register(checkBuildEnvironment)
        }
        if (checks.hasInstance<Check.MacOSLocalhost>() && accessor.isMac) {
            val task = project.tasks.register<MacOSLocalhostResolvingTask>("checkMacOSLocalhostResolving") {
                group = "verification"
                description =
                    "Check macOS localhost resolving issue from Java (https://thoeni.io/post/macos-sierra-java/)"
            }
            checkBuildEnvironment {
                dependsOn(task)
            }
        }
        if (checks.hasInstance<Check.IncrementalKapt>()) {
            val check = checks.getInstance<Check.IncrementalKapt>()
            val task = project.tasks.register<IncrementalKaptTask>("checkIncrementalKapt") {
                group = "verification"
                description = "Check that all annotation processors support incremental kapt if it is turned on"
                mode.set(check.mode)
                this.accessor.set(accessor)
            }
            checkBuildEnvironment {
                dependsOn(task)
            }
        }
    }

    private fun StartParameter.addTaskNames(vararg names: String) {
        // getter returns defensive copy
        setTaskNames(taskNames + names.toList())
    }

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

    private fun lazyCheck(precondition: Boolean, message: () -> String) {
        if (!precondition) {
            validationErrors += message.invoke()
        }
    }

    private fun Project.checkAppliesRequiredPlugin(pluginId: String) {
        lazyCheck(plugins.hasPlugin(pluginId)) {
            "You forgot to apply '$pluginId' plugin to kotlin library module $path. it is required"
        }
    }

    private fun checkGradleProperties(project: Project, accessor: SystemValuesAccessor) {
        project.afterEvaluate {
            val tracker = buildTracker(project)
            val sentry = project.sentry
            val propertiesChecks = listOf(
                GradlePropertiesCheck(project, accessor) // TODO: extract to a task
            )
            propertiesChecks.forEach { checker ->
                checker.getMismatches()
                    .onSuccess {
                        it.forEach { mismatch ->
                            project.logger.warn(
                                "${mismatch.name} differs from recommended value! " +
                                    "Recommended: ${mismatch.expected} " +
                                    "Actual: ${mismatch.actual}"
                            )
                            val safeParamName = mismatch.name.replace(".", "-")
                            tracker.track(CountMetric("configuration.mismatch.$safeParamName"))
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

    private fun printBuildEnvironment(project: Project, accessor: SystemValuesAccessor, logger: Logger) {
        val isBuildCachingEnabled = project.gradle.startParameter.isBuildCacheEnabled
        val minSdk = project.getOptionalStringProperty("minSdk")
        val kaptBuildCache: Boolean = project.getBooleanProperty("kaptBuildCache")
        val kaptMapDiagnosticLocations = project.getBooleanProperty("kaptMapDiagnosticLocations")
        val javaIncrementalCompilation = project.getBooleanProperty("javaIncrementalCompilation")

        logger.info(
            """Config information for project: ${project.displayName}:
BuildEnvironment: ${project.buildEnvironment}
${startParametersDescription(project.gradle)}
java=${accessor.javaInfo}
JAVA_HOME=${accessor.javaHome}
ANDROID_HOME=${AndroidSdk.fromProject(project).androidHome}
org.gradle.caching=$isBuildCachingEnabled
android.enableD8=${project.getOptionalStringProperty("android.enableD8")}
android.enableR8.fullMode=${project.getOptionalStringProperty("android.enableR8.fullMode")}
android.builder.sdkDownload=${project.getOptionalStringProperty("android.builder.sdkDownload")}
kotlin.version=${accessor.kotlinVersion}
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

    private fun startParametersDescription(gradle: Gradle): String =
        gradle.startParameter.toString().replace(',', '\n')
}

private const val pluginName = "BuildParamCheckPlugin"
private const val enabledProp = "avito.build-checks.enabled"
