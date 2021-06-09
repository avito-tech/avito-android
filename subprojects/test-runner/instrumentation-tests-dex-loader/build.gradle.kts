plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.libraries")
    id("convention.test-fixtures")
}

dependencies {
    api(project(":common:report-viewer")) {
        because("TestName model") // todo test models should be separated from reports
    }
    implementation(gradleApi())
    implementation(libs.dexlib)
    implementation(libs.kotson)
    implementation(project(":common:files"))
    implementation(project(":gradle:android")) {
        because("For getApkOrThrow function only")
    }

    testImplementation(project(":common:truth-extensions"))
    testImplementation(project(":common:resources"))
    testImplementation(libs.mockitoKotlin)
    testImplementation(libs.mockitoJUnitJupiter)
}
