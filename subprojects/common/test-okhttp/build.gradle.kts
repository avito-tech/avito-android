plugins {
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    api(Dependencies.test.okhttpMockWebServer)

    implementation(Dependencies.test.truth)
}
