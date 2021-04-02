plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
    id("convention.libraries")
    id("convention.gradle-testing")
}

dependencies {
    api(projects.gradle.buildVerdictTasksApi)

    implementation(gradleApi())
    implementation(projects.common.throwableUtils)
    implementation(projects.gradle.gradleExtensions)
    implementation(projects.gradle.gradleLogger)
    implementation(libs.gson)
    implementation(libs.kotlinHtml)

    gradleTestImplementation(projects.gradle.testProject)
}

gradlePlugin {
    plugins {
        create("buildVerdict") {
            id = "com.avito.android.build-verdict"
            implementationClass = "com.avito.android.build_verdict.BuildVerdictPlugin"
            displayName = "Create file with a build verdict"
        }
    }
}
