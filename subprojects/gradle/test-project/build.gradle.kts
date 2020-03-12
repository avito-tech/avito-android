plugins {
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    api(Dependencies.test.okhttpMockWebServer)

    implementation(project(":subprojects:gradle:process"))
    implementation(project(":subprojects:gradle:utils"))
    implementation(project(":subprojects:gradle:android"))
    implementation(gradleTestKit())
    implementation(Dependencies.kotlinReflect)
    implementation(Dependencies.funktionaleTry)
    implementation(Dependencies.test.truth)

    testImplementation(Dependencies.test.kotlinTest)
    testImplementation(Dependencies.test.kotlinTestJUnit)
}
