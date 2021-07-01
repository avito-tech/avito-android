import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("convention.libraries")
    id("convention.unit-testing")
}

dependencies {
    add("implementation", libs.kotlinStdlib)
}

/**
 * 1.4 used because of kotlin dsl bundled version
 * todo use 1.5
 */
val kotlinLanguageVersion = "1.4"

/**
 * Exists because `compile` task ambiguous in projects with jvm and android modules combined
 */
val compileAllTask: TaskProvider<Task> = tasks.register("compileAll") {
    description = "Compiles all available modules in all variants"

    dependsOn(tasks.withType<KotlinCompile>())
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {

        jvmTarget = libs.javaVersion.toString()

        /**
         * Can't enable because of warning
         * "Runtime JAR files in the classpath should have the same version. These files were found in the classpath:"
         * Gradle has 1.4.x bundled
         */
        allWarningsAsErrors = false

        languageVersion = kotlinLanguageVersion
        apiVersion = kotlinLanguageVersion

        freeCompilerArgs = freeCompilerArgs +
            "-Xopt-in=kotlin.RequiresOptIn" +
            "-progressive"
    }
}

@Suppress("UnstableApiUsage")
val kotlinVersion: String = providers.systemProperty("kotlinVersion")
    .forUseAtConfigurationTime()
    .get()

configurations.all {
    resolutionStrategy.eachDependency {
        if (requested.group == "org.jetbrains.kotlin"
            && requested.name != "kotlin-stdlib-jre8" // deprecated and not updated anymore. It's replaced by jdk
            && requested.name != "kotlin-stdlib-jre7"
        ) {
            useVersion(kotlinVersion)
        }
    }
}
