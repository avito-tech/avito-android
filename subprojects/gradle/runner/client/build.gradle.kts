plugins {
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

extra["artifact-id"] = "runner-client"

dependencies {
    compileOnly(gradleApi())
    api(project(":subprojects:gradle:runner:shared"))
    api(project(":subprojects:gradle:runner:service"))

    implementation(project(":subprojects:gradle:trace-event"))
    implementation(project(":subprojects:common:math"))
    implementation(Dependencies.funktionaleTry)
    implementation(Dependencies.coroutinesCore)
    implementation(Dependencies.gson)

    testImplementation(project(":subprojects:common:logger-test-fixtures"))
    testImplementation(project(":subprojects:common:time-test-fixtures"))
    testImplementation(project(":subprojects:gradle:test-project"))
    testImplementation(project(":subprojects:gradle:runner:shared-test"))
    testImplementation(Dependencies.kotlinReflect)
    testImplementation(Dependencies.Test.mockitoKotlin)
    testImplementation(Dependencies.Test.mockitoJUnitJupiter)
    testImplementation(Dependencies.Test.coroutinesTest)
}
