package com.avito.android.plugin.build_param_check

import com.avito.android.plugin.build_param_check.BuildChecksExtension.Check.AndroidSdk
import com.avito.android.plugin.build_param_check.BuildChecksExtension.Check.DynamicDependencies
import com.avito.android.plugin.build_param_check.BuildChecksExtension.Check.GradleDaemon
import com.avito.android.plugin.build_param_check.BuildChecksExtension.Check.GradleProperties
import com.avito.android.plugin.build_param_check.BuildChecksExtension.Check.IncrementalKapt
import com.avito.android.plugin.build_param_check.BuildChecksExtension.Check.JavaVersion
import com.avito.android.plugin.build_param_check.BuildChecksExtension.Check.MacOSLocalhost
import com.avito.android.plugin.build_param_check.BuildChecksExtension.Check.ModuleTypes
import com.avito.android.plugin.build_param_check.BuildChecksExtension.Check.UniqueRClasses
import org.gradle.api.Action

open class BuildChecksExtension {

    var enableByDefault: Boolean = true

    internal val checks = mutableSetOf<Check>()

    fun javaVersion(action: Action<JavaVersion>) = register(JavaVersion(), action)

    fun androidSdk(action: Action<AndroidSdk>) = register(AndroidSdk(), action)

    fun gradleDaemon(action: Action<GradleDaemon>) = register(GradleDaemon(), action)

    fun dynamicDependencies(action: Action<DynamicDependencies>) = register(DynamicDependencies(), action)

    fun macOSLocalhost(action: Action<MacOSLocalhost>) = register(MacOSLocalhost(), action)

    fun gradleProperties(action: Action<GradleProperties>) = register(GradleProperties(), action)

    fun moduleTypes(action: Action<ModuleTypes>) = register(ModuleTypes(), action)

    fun uniqueRClasses(action: Action<UniqueRClasses>) = register(UniqueRClasses(), action)

    fun incrementalKapt(action: Action<IncrementalKapt>) = register(IncrementalKapt(), action)

    private fun <T : Check> register(check: T, action: Action<T>) {
        @Suppress("UselessCallOnCollection")
        if (checks.filterIsInstance(check.javaClass).isNotEmpty()) {
            throw IllegalStateException(
                "You can't use ${check.javaClass.simpleName} build check twice. " +
                    "See '$extensionName'."
            )
        }
        action.execute(check)
        checks.add(check)
    }

    interface RequireParameters {
        fun validate() {}
    }

    sealed class Check {

        open var enabled: Boolean = true

        open class AndroidSdk : Check(), RequireParameters {

            var compileSdkVersion: Int = -1
            var revision: Int = -1

            override fun validate() {
                check(compileSdkVersion > 0) { "$extensionName.androidSdk.compileSdkVersion must be set" }
                check(revision > 0) { "$extensionName.androidSdk.revision must be set" }
            }
        }

        open class JavaVersion : Check(), RequireParameters {
            var version: org.gradle.api.JavaVersion? = null

            override fun validate() {
                checkNotNull(version) { "$extensionName.javaVersion.version must be set" }
            }
        }

        open class GradleDaemon : Check()

        open class DynamicDependencies : Check()

        open class MacOSLocalhost : Check()

        open class GradleProperties : Check() {
            override var enabled: Boolean = false
        }

        open class ModuleTypes : Check() {
            override var enabled: Boolean = false
        }

        open class UniqueRClasses : Check()

        open class IncrementalKapt : Check() {
            var mode: CheckMode = CheckMode.WARNING
        }

        override fun equals(other: Any?): Boolean {
            return this.javaClass == other?.javaClass
        }

        override fun hashCode(): Int = 0
    }
}

internal const val extensionName = "buildChecks"
