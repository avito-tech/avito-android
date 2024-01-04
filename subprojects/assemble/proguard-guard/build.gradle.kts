plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
    id("convention.gradle-testing")
}

dependencies {
    implementation(project(":subprojects:common:diff-util"))
    implementation(project(":subprojects:gradle:gradle-extensions"))
    implementation(project(":subprojects:gradle:android"))
    implementation(project(":subprojects:logger:gradle-logger"))
    implementation(libs.proguardBase)

    gradleTestImplementation(project(":subprojects:common:resources"))
    gradleTestImplementation(project(":subprojects:gradle:test-project"))
    gradleTestImplementation(libs.truth)
}

gradlePlugin {
    plugins {
        create("proguardGuardPlugin") {
            id = "com.avito.android.proguard-guard"
            implementationClass = "com.avito.android.proguard_guard.ProguardGuardPlugin"
            displayName = "Locking a merged proguard configuration"
        }
    }
}
