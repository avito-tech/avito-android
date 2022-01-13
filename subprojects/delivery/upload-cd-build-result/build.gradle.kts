plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
    id("convention.gradle-testing")
}

dependencies {
    implementation(gradleApi())
    implementation(libs.okhttp)
    implementation(libs.okhttpLogging)
    implementation(libs.gson)

    implementation(projects.subprojects.common.okhttp)
    implementation(projects.subprojects.gradle.android)
    implementation(projects.subprojects.gradle.buildEnvironment)
    implementation(projects.subprojects.gradle.git)
    implementation(projects.subprojects.gradle.gradleExtensions)

    testImplementation(projects.subprojects.common.testOkhttp)
    testImplementation(testFixtures(projects.subprojects.gradle.git))

    gradleTestImplementation(projects.subprojects.gradle.testProject)
}

gradlePlugin {
    plugins {
        create("cdContract") {
            id = "com.avito.android.upload-cd-build-result"
            implementationClass = "com.avito.cd.UploadCdBuildResultPlugin"
            displayName = "CD Contract Plugin"
        }
    }
}
