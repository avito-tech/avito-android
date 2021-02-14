package com.avito.android.build_checks

import com.avito.android.build_checks.BuildChecksExtension.Check.AndroidSdk
import com.avito.android.build_checks.BuildChecksExtension.Check.DynamicDependencies
import com.avito.android.build_checks.BuildChecksExtension.Check.GradleDaemon
import com.avito.android.build_checks.BuildChecksExtension.Check.GradleProperties
import com.avito.android.build_checks.BuildChecksExtension.Check.IncrementalKapt
import com.avito.android.build_checks.BuildChecksExtension.Check.JavaVersion
import com.avito.android.build_checks.BuildChecksExtension.Check.MacOSLocalhost
import com.avito.android.build_checks.BuildChecksExtension.Check.ModuleTypes
import com.avito.android.build_checks.BuildChecksExtension.Check.UniqueRClasses
import org.gradle.api.Action

public open class BuildChecksExtension {

    public var enableByDefault: Boolean = true

    internal val checks = mutableSetOf<Check>()

    public fun javaVersion(action: Action<JavaVersion>): Unit =
        register(JavaVersion(), action)

    public fun androidSdk(action: Action<AndroidSdk>): Unit =
        register(AndroidSdk(), action)

    public fun gradleDaemon(action: Action<GradleDaemon>): Unit =
        register(GradleDaemon(), action)

    public fun dynamicDependencies(action: Action<DynamicDependencies>): Unit =
        register(DynamicDependencies(), action)

    public fun macOSLocalhost(action: Action<MacOSLocalhost>): Unit =
        register(MacOSLocalhost(), action)

    public fun gradleProperties(action: Action<GradleProperties>): Unit =
        register(GradleProperties(), action)

    public fun moduleTypes(action: Action<ModuleTypes>): Unit =
        register(ModuleTypes(), action)

    public fun uniqueRClasses(action: Action<UniqueRClasses>): Unit =
        register(UniqueRClasses(), action)

    public fun incrementalKapt(action: Action<IncrementalKapt>): Unit =
        register(IncrementalKapt(), action)

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

    internal interface RequireParameters {
        fun validate() {}
    }

    public sealed class Check {

        public open var enabled: Boolean = true

        public open class AndroidSdk : Check(), RequireParameters {

            public var compileSdkVersion: Int = -1
            public var revision: Int = -1

            override fun validate() {
                check(compileSdkVersion > 0) { "$extensionName.androidSdk.compileSdkVersion must be set" }
                check(revision > 0) { "$extensionName.androidSdk.revision must be set" }
            }
        }

        public open class JavaVersion : Check(), RequireParameters {
            public var version: org.gradle.api.JavaVersion? = null

            override fun validate() {
                checkNotNull(version) { "$extensionName.javaVersion.version must be set" }
            }
        }

        public open class GradleDaemon : Check()

        public open class DynamicDependencies : Check()

        public open class MacOSLocalhost : Check()

        public open class GradleProperties : Check() {
            override var enabled: Boolean = false
        }

        public open class ModuleTypes : Check() {
            override var enabled: Boolean = false
        }

        public open class UniqueRClasses : Check() {
            public val allowedNonUniquePackageNames: MutableList<String> = mutableListOf()
        }

        public open class IncrementalKapt : Check() {
            public var mode: CheckMode = CheckMode.WARNING
        }

        override fun equals(other: Any?): Boolean {
            return this.javaClass == other?.javaClass
        }

        override fun hashCode(): Int = 0
    }
}

internal const val extensionName = "buildChecks"
