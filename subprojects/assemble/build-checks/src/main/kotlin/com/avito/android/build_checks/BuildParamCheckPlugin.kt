package com.avito.android.build_checks

import com.avito.android.build_checks.AndroidAppChecksExtension.AndroidAppCheck
import com.avito.android.build_checks.BuildChecksExtension.Check
import com.avito.android.build_checks.BuildChecksExtension.RequireValidation
import com.avito.android.build_checks.RootProjectChecksExtension.RootProjectCheck
import com.avito.android.build_checks.internal.BuildEnvLogger
import com.avito.android.build_checks.internal.BuildEnvironmentInfo
import com.avito.android.build_checks.internal.CheckAndroidSdkVersionTask
import com.avito.android.build_checks.internal.MacOSLocalhostResolvingTask
import com.avito.android.build_checks.internal.RootTaskCreator
import com.avito.android.build_checks.internal.kotlin_daemon.KotlinDaemonFallbackDetector
import com.avito.android.build_checks.internal.params.GradlePropertiesChecker
import com.avito.android.build_checks.internal.unique_app_res.UniqueAppResourcesTaskCreator
import com.avito.android.build_checks.internal.unique_r.UniqueRClassesTaskCreator
import com.avito.android.withAndroidApp
import com.avito.kotlin.dsl.isRoot
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.register

@Suppress("unused")
public open class BuildParamCheckPlugin : Plugin<Project> {

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
        val envInfo = BuildEnvironmentInfo(project.providers)

        project.afterEvaluate {
            BuildEnvLogger(project, envInfo).log()
            val checks = extension.enabledChecks()

            checks.filterIsInstance<RequireValidation>()
                .forEach {
                    it.validate()
                }

            registerRootTasks(
                project = project,
                checks = checks,
                envInfo = envInfo,
            )

            if (checks.hasInstance<RootProjectCheck.JavaVersion>()) {
                checkJavaVersion(checks.getInstance(), envInfo)
            }
            if (checks.hasInstance<RootProjectCheck.GradleProperties>()) {
                GradlePropertiesChecker(project, envInfo).check()
            }
            if (checks.hasInstance<RootProjectCheck.PreventKotlinDaemonFallback>()) {
                KotlinDaemonFallbackDetector().register(project)
            }
        }
    }

    private fun applyForAndroidApp(project: Project) {
        val extension = project.extensions.create<AndroidAppChecksExtension>(extensionName)

        if (!project.pluginIsEnabled) return

        project.afterEvaluate {
            val checks = extension.enabledChecks()

            checks.filterIsInstance<RequireValidation>()
                .forEach {
                    it.validate()
                }

            val rootTask = RootTaskCreator(project).getOrCreate()

            if (checks.hasInstance<AndroidAppCheck.UniqueRClasses>()) {
                UniqueRClassesTaskCreator(project, checks.getInstance())
                    .addTask(rootTask)
            }
            if (checks.hasInstance<AndroidAppCheck.UniqueAppResources>()) {
                UniqueAppResourcesTaskCreator(project, checks.getInstance())
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
        envInfo: BuildEnvironmentInfo,
    ) {
        val rootTask = RootTaskCreator(project).getOrCreate()

        if (checks.hasInstance<RootProjectCheck.AndroidSdk>()) {

            val check = checks.getInstance<RootProjectCheck.AndroidSdk>()

            val task = project.tasks.register<CheckAndroidSdkVersionTask>("checkAndroidSdkVersion") {
                group = "verification"
                description = "Checks sdk versions with expected ones to prevent build cache misses"

                versions.set(check.versions)
            }
            rootTask {
                dependsOn(task)
            }
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
    }
}

internal const val pluginId = "com.avito.android.build-checks"
private const val enabledProp = "avito.build-checks.enabled"
internal const val outputDirName = "avito-build-checks"
