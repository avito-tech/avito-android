plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.libraries")
}

dependencies {
    api(projects.common.reportViewer) {
        because("TestName model") // todo test models should be separated from reports
    }
    implementation(gradleApi())
    implementation(libs.dexlib)
    implementation(libs.kotson)
    implementation(projects.common.files)
    implementation(projects.gradle.android) {
        because("For getApkOrThrow function only")
    }

    testImplementation(projects.common.truthExtensions)
    testImplementation(projects.gradle.instrumentationTestsDexLoaderTestFixtures)
    testImplementation(projects.common.resources)
    testImplementation(libs.mockitoKotlin)
    testImplementation(libs.mockitoJUnitJupiter)
}
