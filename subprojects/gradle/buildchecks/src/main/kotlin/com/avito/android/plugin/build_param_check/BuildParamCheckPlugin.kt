package com.avito.android.plugin.build_param_check

import com.avito.android.androidSdk
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
import com.avito.utils.gradle.buildEnvironment
import com.avito.utils.logging.ciLogger
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

    override fun apply(project: Project) {
        val extension = project.extensions.create<BuildChecksExtension>(extensionName)

        printBuildEnvironment(project)

        check(project.isRoot()) {
            "Plugin must be applied to the root project but was applied to ${project.path}"
        }
        project.afterEvaluate {
            val checks = ChecksFilter(extension).checks()
            checks
                .filterIsInstance<BuildChecksExtension.RequireParameters>()
                .forEach {
                    it.validate()
                }

            registerRequiredTasks(project, checks)

            if (checks.hasInstance<Check.JavaVersion>()) {
                checkJavaVersion(checks.getInstance())
            }
            if (checks.hasInstance<Check.GradleProperties>()) {
                checkGradleProperties(project)
            }
            if (checks.hasInstance<Check.ModuleTypes>()) {
                checkModuleHasRequiredPlugins(project)
            }

            showErrorsIfAny(project)
        }
    }

    private fun checkJavaVersion(check: Check.JavaVersion) {
        check(JavaVersion.current() == check.version) {
            "Only ${check.version} is supported for this project but was ${javaInfo()}. " +
                "Please check java home property or install appropriate JDK."
        }
    }

    private fun registerRequiredTasks(project: Project, checks: List<Check>) {
        val checkBuildEnvironment = project.tasks.register("checkBuildEnvironment") {
            it.group = "verification"
            it.description = "Check typical build problems"
        }
        project.gradle.startParameter.setTaskNames(
            // getter returns defencive copy
            project.gradle.startParameter.taskNames + "checkBuildEnvironment"
        )

        if (checks.hasInstance<Check.AndroidSdk>()) {
            val check = checks.getInstance<Check.AndroidSdk>()
            val task = project.tasks.register<CheckAndroidSdkVersionTask>("checkAndroidSdkVersion") {
                group = "verification"
                description =
                    "Checks sdk version in docker against local one to prevent build cache misses"

                revision.set(check.revision)
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
            check(project.pluginManager.hasPlugin("com.avito.android.impact")) {
                "build check 'uniqueRClasses' requires 'com.avito.android.impact' plugin"
            }
            val task = project.tasks.register<UniqueRClassesTask>("checkUniqueAndroidPackages") {
                group = "verification"
                description = "Verify unique R classes"
            }
            checkBuildEnvironment {
                dependsOn(task)
            }
        }
        if (checks.hasInstance<Check.MacOSLocalhost>() && isMac()) {
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

    private fun checkGradleProperties(project: Project) {
        project.afterEvaluate {
            val tracker = buildTracker(project)
            val sentry = project.sentry
            val propertiesChecks = listOf(
                GradlePropertiesCheck(project) // TODO: extract to a task
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
