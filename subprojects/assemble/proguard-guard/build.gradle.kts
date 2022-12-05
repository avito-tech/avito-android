plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
    id("convention.gradle-testing")
}

dependencies {
    implementation(projects.subprojects.common.diffUtil)
    implementation(projects.subprojects.gradle.gradleExtensions)
    implementation(projects.subprojects.gradle.android)

    gradleTestImplementation(projects.subprojects.common.resources)
    gradleTestImplementation(projects.subprojects.gradle.testProject)
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
