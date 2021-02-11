plugins {
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    api(Dependencies.okhttp)

    implementation(project(":subprojects:common:logger"))
    implementation(Dependencies.okhttpLogging)

    testImplementation(project(":subprojects:common:test-okhttp"))
    testImplementation(project(":subprojects:common:logger-test-fixtures"))
    testImplementation(Dependencies.funktionaleTry)
    testImplementation(Dependencies.retrofit)
    testImplementation(Dependencies.retrofitConverterGson)
}

kotlin {
    explicitApi()
}
