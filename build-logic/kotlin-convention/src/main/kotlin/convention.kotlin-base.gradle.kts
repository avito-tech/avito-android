import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("convention.libraries")
    id("convention.unit-testing")
}

dependencies {
    add("implementation", libs.kotlinStdlib)
}

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
        allWarningsAsErrors = true
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
