plugins {
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    api(Dependencies.test.okhttpMockWebServer)

    implementation(Dependencies.test.truth)
    implementation(Dependencies.gson)
    implementation(Dependencies.commonsLang)

    implementation(project(":common:junit-utils"))
    implementation(project(":common:waiter"))
}
