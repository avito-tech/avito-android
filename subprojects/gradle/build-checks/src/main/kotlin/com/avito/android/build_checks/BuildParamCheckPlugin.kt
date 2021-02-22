package com.avito.android.build_checks

import com.avito.android.build_checks.AndroidAppChecksExtension.AndroidAppCheck
import com.avito.android.build_checks.BuildChecksExtension.Check
import com.avito.android.build_checks.BuildChecksExtension.RequireValidation
import com.avito.android.build_checks.RootProjectChecksExtension.RootProjectCheck
import com.avito.android.build_checks.internal.BuildEnvLogger
import com.avito.android.build_checks.internal.BuildEnvironmentInfo
import com.avito.android.build_checks.internal.CheckAndroidSdkVersionTask
import com.avito.android.build_checks.internal.CheckGradleDaemonTask
import com.avito.android.build_checks.internal.DynamicDependenciesTask
import com.avito.android.build_checks.internal.params.GradlePropertiesChecker
import com.avito.android.build_checks.internal.MacOSLocalhostResolvingTask
import com.avito.android.build_checks.internal.incremental_kapt.IncrementalKaptTask
import com.avito.android.build_checks.internal.unique_r.UniqueRClassesTaskProvider
import com.avito.android.withAndroidApp
import com.avito.kotlin.dsl.isRoot
import com.avito.logger.GradleLoggerFactory
import com.avito.logger.Logger
import org.gradle.StartParameter
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.register
import org.gradle.tooling.BuildException

@Suppress("unused")
public open class BuildParamCheckPlugin : Plugin<Project> {

    private val validationErrors = mutableListOf<String>()

    @Suppress("UnstableApiUsage")
    private val Project.pluginIsEnabled: Boolean
        get() = providers
            .gradleProperty(enabledProp)
            .forUseAtConfigurationTime()
            .map { it.toBoolean() }
            .getOrElse(true)

    override fun apply(project: Project) {
        if (project.isRoot()) {
            applyForRootProject(project)
        } else {
            project.withAndroidApp {
                applyForAndroidApp(project)
            }
            project.afterEvaluate {
                require(it.pluginManager.hasPlugin("com.android.application")) {
                    "$pluginId plugin can be applied only for root or Android app project. " +
                        "It is applied in project ${project.path}"
                }
            }
        }
    }

    private fun applyForRootProject(project: Project) {
        val extension = project.extensions.create<RootProjectChecksExtension>(extensionName)

        if (!project.pluginIsEnabled) return

        val logger = GradleLoggerFactory.getLogger(this, project)
        val envInfo = BuildEnvironmentInfo(project.providers)

        BuildEnvLogger(project, logger, envInfo).log()

        project.afterEvaluate {
            val checks = extension.enabledChecks()

            checks.filterIsInstance<RequireValidation>()
                .forEach {
                    it.validate()
                }

            registerRootTasks(project, checks, logger, envInfo)

            if (checks.hasInstance<RootProjectCheck.JavaVersion>()) {
                checkJavaVersion(checks.getInstance(), envInfo)
            }
            if (checks.hasInstance<RootProjectCheck.GradleProperties>()) {
                GradlePropertiesChecker(project, envInfo).check()
            }
            if (checks.hasInstance<RootProjectCheck.ModuleTypes>()) {
                checkModuleHasRequiredPlugins(project)
            }

            showErrorsIfAny(project)
        }
    }

    private fun applyForAndroidApp(project: Project) {
        require(project.rootProject.plugins.hasPlugin(pluginId)) {
            "$pluginId plugin must be applied also to the root project"
        }
        val extension = project.extensions.create<AndroidAppChecksExtension>(extensionName)

        if (!project.pluginIsEnabled) return

        project.afterEvaluate {
            val checks = extension.enabledChecks()

            checks.filterIsInstance<RequireValidation>()
                .forEach {
                    it.validate()
                }

            val rootTask = project.rootProject.tasks.named(rootTaskName)

            if (checks.hasInstance<AndroidAppCheck.UniqueRClasses>()) {
                UniqueRClassesTaskProvider(project, checks.getInstance())
                    .addTask(rootTask)
            }
        }
    }

    private fun checkJavaVersion(check: RootProjectCheck.JavaVersion, envInfo: BuildEnvironmentInfo) {
        check(JavaVersion.current() == check.version) {
            "Only ${check.version} is supported for this project but was ${envInfo.javaInfo}. " +
                "Please check java home property or install appropriate JDK."
        }
    }

    private fun registerRootTasks(
        project: Project,
        checks: List<Check>,
        logger: Logger,
        envInfo: BuildEnvironmentInfo,
    ) {
        val rootTask = project.tasks.register(rootTaskName) {
            it.group = "verification"
            it.description = "Check typical build problems"
        }
        project.gradle.startParameter.addTaskNames(":checkBuildEnvironment")

        if (checks.hasInstance<RootProjectCheck.AndroidSdk>()) {
            val check = checks.getInstance<RootProjectCheck.AndroidSdk>()
            val task = project.tasks.register<CheckAndroidSdkVersionTask>("checkAndroidSdkVersion") {
                group = "verification"
                description = "Checks sdk version in docker against local one to prevent build cache misses"

                compileSdkVersion.set(check.compileSdkVersion)
                platformRevision.set(check.revision)
                // don't run task if it is already compared hashes and it's ok
                // task will be executed next time if either local jar or docker jar(e.g. inputs) changed
                outputs.upToDateWhen { outputs.files.singleFile.exists() }
            }
            rootTask {
                dependsOn(task)
            }
        }
        if (checks.hasInstance<RootProjectCheck.GradleDaemon>()) {
            val task = project.tasks.register<CheckGradleDaemonTask>("checkGradleDaemon") {
                group = "verification"
                description = "Check gradle daemon problems"
            }
            rootTask {
                dependsOn(task)
            }
        }
        if (checks.hasInstance<RootProjectCheck.DynamicDependencies>()) {
            val task = project.tasks.register<DynamicDependenciesTask>("checkDynamicDependencies") {
                group = "verification"
                description = "Detects dynamic dependencies"
            }
            rootTask {
                dependsOn(task)
            }
        }
        if (checks.hasInstance<RootProjectCheck.UniqueRClasses>()) {
            logger.warn("Build check '${RootProjectChecksExtension::uniqueRClasses.name}' is moved to Android app module")
        }
        if (checks.hasInstance<RootProjectCheck.MacOSLocalhost>() && envInfo.isMac) {
            val task = project.tasks.register<MacOSLocalhostResolvingTask>("checkMacOSLocalhostResolving") {
                group = "verification"
                description =
                    "Check macOS localhost resolving issue from Java (https://thoeni.io/post/macos-sierra-java/)"
            }
            rootTask {
                dependsOn(task)
            }
        }
        if (checks.hasInstance<RootProjectCheck.IncrementalKapt>()) {
            val check = checks.getInstance<RootProjectCheck.IncrementalKapt>()
            val task = project.tasks.register<IncrementalKaptTask>("checkIncrementalKapt") {
                group = "verification"
                description = "Check that all annotation processors support incremental kapt if it is turned on"
                mode.set(check.mode)
                this.accessor.set(envInfo)
            }
            rootTask {
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
}

internal const val pluginId = "com.avito.android.build-checks"
private const val enabledProp = "avito.build-checks.enabled"
private const val rootTaskName = "checkBuildEnvironment"
