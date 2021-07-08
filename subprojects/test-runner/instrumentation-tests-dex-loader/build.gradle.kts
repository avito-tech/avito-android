plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.test-fixtures")
}

dependencies {
    api(projects.testRunner.testModel)
    api(projects.common.result)

    implementation(gradleApi())
    implementation(libs.dexlib)

    testImplementation(projects.common.truthExtensions)
    testImplementation(projects.common.resources)
    testImplementation(libs.mockitoKotlin)
    testImplementation(libs.mockitoJUnitJupiter)
}
