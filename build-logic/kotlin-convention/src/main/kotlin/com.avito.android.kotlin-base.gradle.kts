import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("com.avito.android.libraries")
    id("com.avito.android.unit-testing")
}

dependencies {
    add("implementation", platform("com.avito.android.infra:platforms"))
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = libs.javaVersion.toString()
        allWarningsAsErrors = true
        freeCompilerArgs = freeCompilerArgs + "-Xopt-in=kotlin.RequiresOptIn"
    }
}
