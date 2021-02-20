plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.test-fixtures")
    id("convention.integration-testing")
    id("convention.libraries")
}

publish {
    artifactId.set("runner-device-provider")
}

dependencies {
    api(project(":subprojects:gradle:runner:service"))
    api(project(":subprojects:gradle:runner:shared"))
    api(project(":subprojects:gradle:runner:stub"))
    api(project(":subprojects:gradle:kubernetes"))

    implementation(libs.funktionaleTry)
    implementation(project(":subprojects:gradle:process"))
    implementation(project(":subprojects:common:logger"))

    integTestImplementation(project(":subprojects:common:truth-extensions"))

    testImplementation(testFixtures(project(":subprojects:common:logger")))
    testImplementation(libs.coroutinesTest)
}

kotlin {
    explicitApi()
}
