plugins {
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    api(Dependencies.Test.okhttpMockWebServer)

    implementation(Dependencies.Test.truth)
    implementation(Dependencies.gson)
    implementation(Dependencies.commonsLang)

    implementation(project(":common:junit-utils"))
    implementation(project(":common:waiter"))
}
