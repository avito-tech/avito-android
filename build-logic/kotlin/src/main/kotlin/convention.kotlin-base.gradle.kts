import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("convention.unit-testing")
}

val kotlinLanguageVersion = "1.5"

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

        allWarningsAsErrors = false

        languageVersion = kotlinLanguageVersion
        apiVersion = kotlinLanguageVersion

        freeCompilerArgs = freeCompilerArgs +
            "-Xopt-in=kotlin.RequiresOptIn" +
            "-progressive"
    }
}
