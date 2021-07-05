import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("convention.unit-testing")
}

/**
 * overrides 1.4, that comes from kotlin dsl bundled version
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

        jvmTarget = JavaVersion.VERSION_1_8.toString()

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
