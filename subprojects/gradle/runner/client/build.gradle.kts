plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.libraries")
}

publish {
    artifactId.set("runner-client")
}

dependencies {
    compileOnly(gradleApi())
    api(project(":gradle:runner:shared"))
    api(project(":gradle:runner:service"))

    implementation(project(":gradle:trace-event"))
    implementation(project(":common:math"))
    implementation(libs.funktionaleTry)
    implementation(libs.coroutinesCore)
    implementation(libs.gson)

    testImplementation(project(":common:truth-extensions"))
    testImplementation(project(":gradle:runner:shared-test"))
    testImplementation(project(":gradle:test-project"))
    testImplementation(testFixtures(project(":common:logger")))
    testImplementation(testFixtures(project(":common:time")))
    testImplementation(libs.kotlinReflect)
    testImplementation(libs.mockitoKotlin)
    testImplementation(libs.mockitoJUnitJupiter)
    testImplementation(libs.coroutinesTest)
}
