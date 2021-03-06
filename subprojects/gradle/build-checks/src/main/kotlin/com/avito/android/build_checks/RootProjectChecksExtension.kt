package com.avito.android.build_checks

import com.avito.android.build_checks.RootProjectChecksExtension.RootProjectCheck.AndroidSdk
import com.avito.android.build_checks.RootProjectChecksExtension.RootProjectCheck.DynamicDependencies
import com.avito.android.build_checks.RootProjectChecksExtension.RootProjectCheck.GradleDaemon
import com.avito.android.build_checks.RootProjectChecksExtension.RootProjectCheck.GradleProperties
import com.avito.android.build_checks.RootProjectChecksExtension.RootProjectCheck.IncrementalKapt
import com.avito.android.build_checks.RootProjectChecksExtension.RootProjectCheck.JavaVersion
import com.avito.android.build_checks.RootProjectChecksExtension.RootProjectCheck.MacOSLocalhost
import com.avito.android.build_checks.RootProjectChecksExtension.RootProjectCheck.ModuleTypes
import org.gradle.api.Action
import kotlin.reflect.full.createInstance

public open class RootProjectChecksExtension : BuildChecksExtension() {

    override val allChecks: List<Check>
        get() {
            return RootProjectCheck::class.sealedSubclasses
                .map { it.createInstance() }
        }

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

    public fun incrementalKapt(action: Action<IncrementalKapt>): Unit =
        register(IncrementalKapt(), action)

    public sealed class RootProjectCheck : Check {

        public override var enabled: Boolean = true

        public open class AndroidSdk : RootProjectCheck(), RequireValidation {

            public var compileSdkVersion: Int = -1
            public var revision: Int = -1

            override fun validate() {
                check(compileSdkVersion > 0) { "$extensionName.androidSdk.compileSdkVersion must be set" }
                check(revision > 0) { "$extensionName.androidSdk.revision must be set" }
            }
        }

        public open class JavaVersion : RootProjectCheck(), RequireValidation {
            public var version: org.gradle.api.JavaVersion? = null

            override fun validate() {
                checkNotNull(version) { "$extensionName.javaVersion.version must be set" }
            }
        }

        public open class GradleDaemon : RootProjectCheck()

        public open class DynamicDependencies : RootProjectCheck()

        public open class MacOSLocalhost : RootProjectCheck()

        public open class GradleProperties : RootProjectCheck() {
            override var enabled: Boolean = false
        }

        public open class ModuleTypes : RootProjectCheck() {
            override var enabled: Boolean = false
        }

        public open class IncrementalKapt : RootProjectCheck() {
            public var mode: CheckMode = CheckMode.WARNING
        }

        override fun equals(other: Any?): Boolean {
            return this.javaClass == other?.javaClass
        }
    }
}
