plugins {
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

extra["artifact-id"] = "runner-service"

dependencies {
    api(project(":subprojects:common:coroutines-extension"))
    api(project(":subprojects:common:statsd"))
    api(project(":subprojects:common:time"))
    implementation(project(":subprojects:gradle:runner:shared"))
    implementation(Dependencies.funktionaleTry)
    implementation(Dependencies.AndroidTest.ddmlib)
    implementation(Dependencies.rxJava)

    testImplementation(project(":subprojects:common:logger-test-fixtures"))
    testImplementation(project(":subprojects:common:time-test-fixtures"))
    testImplementation(project(":subprojects:common:files"))
    testImplementation(project(":subprojects:common:truth-extensions"))
    testImplementation(project(":subprojects:common:resources"))
    testImplementation(project(":subprojects:gradle:test-project"))
    testImplementation(project(":subprojects:gradle:runner:shared-test"))
    testImplementation(Dependencies.kotlinReflect)
    testImplementation(Dependencies.Test.truth)
    testImplementation(Dependencies.Test.mockitoKotlin)
    testImplementation(Dependencies.Test.mockitoJUnitJupiter)
    testImplementation(Dependencies.Test.coroutinesTest)
}
