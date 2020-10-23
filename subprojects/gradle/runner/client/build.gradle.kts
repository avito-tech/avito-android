plugins {
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

extra["artifact-id"] = "runner-client"

dependencies {
    compileOnly(gradleApi())
    api(project(":gradle:runner:shared"))
    api(project(":gradle:runner:service"))

    implementation(project(":gradle:trace-event"))
    implementation(Dependencies.funktionaleTry)
    implementation(Dependencies.coroutinesCore)
    implementation(Dependencies.gson)

    testImplementation(project(":common:logger-test-fixtures"))
    testImplementation(project(":gradle:test-project"))
    testImplementation(project(":gradle:runner:shared-test"))
    testImplementation(Dependencies.kotlinReflect)
    testImplementation(Dependencies.test.mockitoKotlin)
    testImplementation(Dependencies.test.mockitoJUnitJupiter)
    testImplementation(Dependencies.test.coroutinesTest)
}
