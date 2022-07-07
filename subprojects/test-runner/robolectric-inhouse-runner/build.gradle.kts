plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.unit-testing")
}

dependencies {
    api(libs.robolectric)
    api(libs.junit)

    implementation(projects.subprojects.testRunner.testReportRunListener)

    testImplementation(libs.mockitoKotlin)
}
