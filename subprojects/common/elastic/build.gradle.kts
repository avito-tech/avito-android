plugins {
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    api(Dependencies.okhttp)

    implementation(project(":subprojects:common:time"))
    implementation(project(":subprojects:common:okhttp"))
    implementation(project(":subprojects:common:slf4j-logger"))

    implementation(Dependencies.gson)
    implementation(Dependencies.retrofit)
    implementation(Dependencies.retrofitConverterGson)

    testImplementation(project(":subprojects:common:test-okhttp"))
    testImplementation(project(":subprojects:common:time-test-fixtures"))
    testImplementation(project(":subprojects:common:logger-test-fixtures"))
}

kotlin {
    explicitApi()
}
