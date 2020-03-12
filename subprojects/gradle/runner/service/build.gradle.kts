plugins {
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

extra["artifact-id"] = "runner-service"

dependencies {
    compileOnly(gradleApi())
    implementation(project(":subprojects:gradle:runner:shared"))
    implementation(project(":subprojects:gradle:utils"))
    implementation(Dependencies.funktionaleTry)
    implementation(Dependencies.androidTest.ddmlib)
    implementation(Dependencies.coroutinesCore)
    implementation(Dependencies.rxJava)

    testImplementation(project(":subprojects:gradle:test-project"))
    testImplementation(project(":subprojects:gradle:runner:shared-test"))
    testImplementation(Dependencies.kotlinReflect)
    testImplementation(Dependencies.test.truth)
    testImplementation(Dependencies.test.mockitoKotlin)
    testImplementation(Dependencies.test.mockitoJUnitJupiter)
}
