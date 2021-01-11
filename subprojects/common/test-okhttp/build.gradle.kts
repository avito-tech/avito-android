plugins {
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    api(Dependencies.Test.okhttpMockWebServer)
    api(project(":common:logger"))

    implementation(Dependencies.Test.truth)
    implementation(Dependencies.gson)
    implementation(Dependencies.commonsLang)

    implementation(project(":common:junit-utils"))
    implementation(project(":common:resources"))
    implementation(project(":common:waiter"))

    testImplementation(project(":common:logger-test-fixtures"))
}
