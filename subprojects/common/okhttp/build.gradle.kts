plugins {
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    implementation(Dependencies.okhttp)
    implementation(project(":subprojects:common:logger"))
    testImplementation(project(":subprojects:common:test-okhttp"))
    testImplementation(Dependencies.funktionaleTry)
    testImplementation(Dependencies.test.okhttpMockWebServer)
    testImplementation(Dependencies.retrofit)
    testImplementation(Dependencies.retrofitConverterGson)
}
