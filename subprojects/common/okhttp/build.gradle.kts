plugins {
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    api(Dependencies.okhttp)

    implementation(project(":common:logger"))
    testImplementation(project(":common:test-okhttp"))
    testImplementation(Dependencies.funktionaleTry)
    testImplementation(Dependencies.retrofit)
    testImplementation(Dependencies.retrofitConverterGson)
}
