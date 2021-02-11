plugins {
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    api(Dependencies.Test.okhttpMockWebServer)
    api(gradleTestKit())

    implementation(project(":subprojects:gradle:process"))
    implementation(project(":subprojects:gradle:android"))
    implementation(project(":subprojects:common:truth-extensions"))
    implementation(project(":subprojects:common:logger-test-fixtures"))

    implementation(Dependencies.kotlinReflect)
    implementation(Dependencies.funktionaleTry)
    implementation(Dependencies.Test.truth)

    testImplementation(Dependencies.Test.kotlinTest)
    testImplementation(Dependencies.Test.kotlinTestJUnit)
}
