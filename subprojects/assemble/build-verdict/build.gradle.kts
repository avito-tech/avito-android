plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
    id("convention.gradle-testing")
}

dependencies {
    api(projects.subprojects.assemble.buildVerdictTasksApi)

    implementation(gradleApi())
    implementation(projects.subprojects.common.throwableUtils)
    implementation(projects.subprojects.gradle.gradleExtensions)
    implementation(libs.gson)
    implementation(libs.kotlinHtml)

    gradleTestImplementation(projects.subprojects.gradle.testProject)
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
