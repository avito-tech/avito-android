import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("convention.libraries")
    id("convention.unit-testing")
}

dependencies {
    add("implementation", platform("com.avito.android.infra:platforms"))
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = "1.8"
        allWarningsAsErrors = true
        freeCompilerArgs = freeCompilerArgs + "-Xopt-in=kotlin.RequiresOptIn"
    }
}
