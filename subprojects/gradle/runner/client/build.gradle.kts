plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.libraries")
}

extra["artifact-id"] = "runner-client"

dependencies {
    compileOnly(gradleApi())
    api(project(":subprojects:gradle:runner:shared"))
    api(project(":subprojects:gradle:runner:service"))

    implementation(project(":subprojects:gradle:trace-event"))
    implementation(project(":subprojects:common:math"))
    implementation(libs.funktionaleTry)
    implementation(libs.coroutinesCore)
    implementation(libs.gson)

    testImplementation(testFixtures(project(":subprojects:common:logger")))
    testImplementation(testFixtures(project(":subprojects:common:time")))
    testImplementation(project(":subprojects:gradle:test-project"))
    testImplementation(project(":subprojects:gradle:runner:shared-test"))
    testImplementation(libs.kotlinReflect)
    testImplementation(libs.mockitoKotlin)
    testImplementation(libs.mockitoJUnitJupiter)
    testImplementation(libs.coroutinesTest)
}
