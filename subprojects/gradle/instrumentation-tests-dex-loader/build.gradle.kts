plugins {
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    api(project(":subprojects:common:report-viewer")) {
        because("TestName model") // todo test models should be separated from reports
    }
    implementation(gradleApi())
    implementation(Dependencies.dexlib)
    implementation(Dependencies.kotson)
    implementation(project(":subprojects:common:files"))
    implementation(project(":subprojects:gradle:android")) {
        because("For getApkOrThrow function only")
    }
    testImplementation(project(":subprojects:gradle:instrumentation-tests-dex-loader-test-fixtures"))
    testImplementation(project(":subprojects:common:resources"))
    testImplementation(Dependencies.Test.mockitoKotlin)
    testImplementation(Dependencies.Test.mockitoJUnitJupiter)
}
