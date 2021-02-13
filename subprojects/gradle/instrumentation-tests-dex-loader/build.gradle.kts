plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.libraries")
}

dependencies {
    api(project(":subprojects:common:report-viewer")) {
        because("TestName model") // todo test models should be separated from reports
    }
    implementation(gradleApi())
    implementation(libs.dexlib)
    implementation(libs.kotson)
    implementation(project(":subprojects:common:files"))
    implementation(project(":subprojects:gradle:android")) {
        because("For getApkOrThrow function only")
    }
    testImplementation(project(":subprojects:gradle:instrumentation-tests-dex-loader-test-fixtures"))
    testImplementation(project(":subprojects:common:resources"))
    testImplementation(libs.mockitoKotlin)
    testImplementation(libs.mockitoJUnitJupiter)
}
