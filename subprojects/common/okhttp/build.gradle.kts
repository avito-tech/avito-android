plugins {
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    api(Dependencies.okhttp)

    implementation(project(":common:logger"))
    implementation(Dependencies.okhttpLogging)

    testImplementation(project(":common:test-okhttp"))
    testImplementation(project(":common:logger-test-fixtures"))
    testImplementation(Dependencies.funktionaleTry)
    testImplementation(Dependencies.retrofit)
    testImplementation(Dependencies.retrofitConverterGson)
}
