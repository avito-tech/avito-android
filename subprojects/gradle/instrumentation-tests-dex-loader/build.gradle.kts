plugins {
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    api(project(":common:report-viewer")) {
        because("TestName model") // todo test models should be separated from reports
    }
    implementation(Dependencies.dexlib)

    testImplementation(project(":gradle:instrumentation-tests-dex-loader-test-fixtures"))
    testImplementation(project(":gradle:test-project")) {
        because("single fileFromJarResources function") // todo separate from test kit utils
    }
    testImplementation(Dependencies.Test.mockitoKotlin)
    testImplementation(Dependencies.Test.mockitoJUnitJupiter)
}
