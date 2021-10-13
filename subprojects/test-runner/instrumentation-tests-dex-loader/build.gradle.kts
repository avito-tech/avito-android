plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.test-fixtures")
}

dependencies {
    api(projects.subprojects.testRunner.testModel)
    api(projects.subprojects.common.result)

    implementation(gradleApi())
    implementation(libs.dexlib)

    testImplementation(projects.subprojects.common.truthExtensions)
    testImplementation(projects.subprojects.common.resources)
    testImplementation(libs.mockitoKotlin)
    testImplementation(libs.mockitoJUnitJupiter)
}
