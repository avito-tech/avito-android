plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
    id("convention.gradle-testing")
}

dependencies {
    implementation(gradleApi())
    implementation(projects.gradle.criticalPath.api)
    implementation(projects.logger.gradleLogger)
    implementation(libs.gson)
    implementation(projects.gradle.gradleExtensions)

    gradleTestImplementation(projects.common.junitUtils)
    gradleTestImplementation(projects.gradle.testProject)
    gradleTestImplementation(testFixtures(projects.logger.logger))
}

kotlin {
    explicitApi()
}

gradlePlugin {
    plugins {
        create("criticalPath") {
            id = "com.avito.android.critical-path"
            implementationClass = "com.avito.android.critical_path.CriticalPathPlugin"
            displayName = "Build critical path"
            description = "Calculates critical path of a build. These are tasks that define build duration."
        }
    }
}
