plugins {
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    implementation(gradleApi())

    implementation(project(":subprojects:gradle:gradle-logger"))
    implementation(project(":subprojects:common:okhttp"))
    implementation(project(":subprojects:gradle:git"))
    implementation(project(":subprojects:gradle:impact-shared"))
    implementation(project(":subprojects:gradle:gradle-extensions"))
    implementation(Dependencies.retrofit)
    implementation(Dependencies.retrofitConverterGson)
    implementation(Dependencies.okhttp)
    implementation(Dependencies.okhttpLogging)
    implementation(Dependencies.sentry)
    implementation(Dependencies.funktionaleTry)

    testImplementation(project(":subprojects:gradle:test-project"))
    testImplementation(project(":subprojects:common:test-okhttp"))
    testImplementation(project(":subprojects:common:logger-test-fixtures"))
}
