plugins {
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

extra["artifact-id"] = "runner-service"

dependencies {
    api(project(":common:coroutines-extension"))
    api(project(":common:statsd"))
    api(project(":common:time"))
    implementation(project(":gradle:runner:shared"))
    implementation(Dependencies.funktionaleTry)
    implementation(Dependencies.AndroidTest.ddmlib)
    implementation(Dependencies.rxJava)

    testImplementation(project(":common:logger-test-fixtures"))
    testImplementation(project(":common:files"))
    testImplementation(project(":common:truth-extensions"))
    testImplementation(project(":common:resources"))
    testImplementation(project(":gradle:test-project"))
    testImplementation(project(":gradle:runner:shared-test"))
    testImplementation(Dependencies.kotlinReflect)
    testImplementation(Dependencies.Test.truth)
    testImplementation(Dependencies.Test.mockitoKotlin)
    testImplementation(Dependencies.Test.mockitoJUnitJupiter)
    testImplementation(Dependencies.Test.coroutinesTest)
}
