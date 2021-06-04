plugins {
    `kotlin-dsl`
}

group = "com.avito.android.buildlogic"

kotlinDslPluginOptions {
    experimentalWarning.set(false)
}

@Suppress("UnstableApiUsage")
val isCi: Provider<Boolean> = project.providers.gradleProperty("ci")
    .forUseAtConfigurationTime()
    .map { it.toBoolean() }
    .orElse(false)

// Possible workaround for: https://github.com/gradle/gradle/issues/15214
// TODO: Remove after Gradle 7.1 in MBS-10881
tasks
    .matching {
        it.name == "generatePrecompiledScriptPluginAccessors"
    }
    .configureEach {
        if (isCi.get()) {
            outputs.upToDateWhen { false }
            outputs.cacheIf { false }
        }
    }
