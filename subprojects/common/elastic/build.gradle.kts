plugins {
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    api(Dependencies.okhttp)

    implementation(project(":common:time"))
    implementation(project(":common:okhttp"))
    implementation(project(":common:slf4j-logger"))

    implementation(Dependencies.gson)
    implementation(Dependencies.retrofit)
    implementation(Dependencies.retrofitConverterGson)

    testImplementation(project(":common:test-okhttp"))
    testImplementation(project(":common:time-test-fixtures"))
    testImplementation(project(":common:logger-test-fixtures"))
}

kotlin {
    explicitApi()
}
