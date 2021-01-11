plugins {
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    implementation(gradleApi())

    implementation(project(":gradle:gradle-logger"))
    implementation(project(":common:okhttp"))
    implementation(project(":gradle:git"))
    implementation(project(":gradle:impact-shared"))
    implementation(project(":gradle:gradle-extensions"))
    implementation(Dependencies.retrofit)
    implementation(Dependencies.retrofitConverterGson)
    implementation(Dependencies.okhttp)
    implementation(Dependencies.okhttpLogging)
    implementation(Dependencies.sentry)
    implementation(Dependencies.funktionaleTry)

    testImplementation(project(":gradle:test-project"))
    testImplementation(project(":common:test-okhttp"))
    testImplementation(project(":common:logger-test-fixtures"))
}
