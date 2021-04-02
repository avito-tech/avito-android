plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
    id("convention.libraries")
    id("convention.gradle-testing")
}

dependencies {
    implementation(projects.gradle.impactShared)
    implementation(projects.gradle.preBuild)
    implementation(projects.gradle.gradleExtensions)
    implementation(projects.gradle.impact)
    implementation(libs.kotlinPlugin)

    gradleTestImplementation(projects.gradle.testProject)
}

gradlePlugin {
    plugins {
        create("moduleTypes") {
            id = "com.avito.android.module-types"
            implementationClass = "com.avito.android.ModuleTypesPlugin"
            displayName = "Module types"
        }
    }
}
