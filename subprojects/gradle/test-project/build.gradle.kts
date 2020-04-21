plugins {
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    api(Dependencies.test.okhttpMockWebServer)

    implementation(project(":gradle:process"))
    implementation(project(":gradle:utils"))
    implementation(project(":gradle:android"))
    implementation(gradleTestKit())
    implementation(Dependencies.kotlinReflect)
    implementation(Dependencies.funktionaleTry)
    implementation(Dependencies.test.truth)

    testImplementation(Dependencies.test.kotlinTest)
    testImplementation(Dependencies.test.kotlinTestJUnit)
}
