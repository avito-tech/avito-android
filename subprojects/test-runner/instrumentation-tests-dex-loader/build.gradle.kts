plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.test-fixtures")
}

dependencies {
    api(project(":subprojects:test-runner:test-model"))
    api(project(":subprojects:common:result"))

    implementation(gradleApi())
    implementation(libs.dexlib)

    testImplementation(project(":subprojects:common:truth-extensions"))
    testImplementation(project(":subprojects:common:resources"))
    testImplementation(libs.mockitoKotlin)
    testImplementation(libs.mockitoJUnitJupiter)
}
