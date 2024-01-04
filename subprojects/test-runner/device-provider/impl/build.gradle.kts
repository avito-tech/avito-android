plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.test-fixtures")
}

dependencies {
    api(project(":subprojects:test-runner:service"))
    api(project(":subprojects:test-runner:device-provider:api"))
    api(project(":subprojects:test-runner:kubernetes"))

    implementation(project(":subprojects:gradle:process"))
    implementation(project(":subprojects:logger:logger"))
    implementation(project(":subprojects:common:result"))
    implementation(project(":subprojects:common:waiter"))

    testImplementation(libs.coroutinesTest)
    testImplementation(testFixtures(project(":subprojects:common:time")))

    testFixturesApi(testFixtures(project(":subprojects:test-runner:kubernetes")))
}
