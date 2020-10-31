plugins {
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    api(Dependencies.Test.okhttpMockWebServer)
    api(gradleTestKit())

    implementation(project(":gradle:process"))
    implementation(project(":gradle:utils"))
    implementation(project(":gradle:android"))

    implementation(Dependencies.kotlinReflect)
    implementation(Dependencies.funktionaleTry)
    implementation(Dependencies.Test.truth)

    testImplementation(Dependencies.Test.kotlinTest)
    testImplementation(Dependencies.Test.kotlinTestJUnit)
}
