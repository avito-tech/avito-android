plugins {
    `java-platform`
    id("com.avito.android.libraries")
}

// Allow dependencies for dependencies to other platforms (BOMs)
javaPlatform.allowDependencies()

group = "com.avito.android.infra"

dependencies {
    constraints {
        api(libs.kotlinStdlib) { version { prefer(libs.kotlinVersion) } }
    }
}
