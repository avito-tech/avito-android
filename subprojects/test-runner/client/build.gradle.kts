plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.libraries")
    id("convention.test-fixtures")
}

publish {
    artifactId.set("runner-client")
}

dependencies {
    compileOnly(gradleApi())
    api(project(":test-runner:shared"))
    api(project(":test-runner:service"))

    implementation(project(":gradle:trace-event"))
    implementation(project(":common:math"))
    implementation(project(":common:result"))
    implementation(project(":common:files"))
    implementation(libs.coroutinesCore)
    implementation(libs.gson)

    testImplementation(project(":common:truth-extensions"))
    testImplementation(project(":test-runner:shared-test"))
    testImplementation(project(":gradle:test-project"))
    testImplementation(testFixtures(project(":common:logger")))
    testImplementation(testFixtures(project(":common:time")))
    testImplementation(libs.kotlinReflect)
    testImplementation(libs.mockitoKotlin)
    testImplementation(libs.mockitoJUnitJupiter)
    testImplementation(libs.coroutinesTest)
}
