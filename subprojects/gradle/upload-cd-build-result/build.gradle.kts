plugins {
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    implementation(gradleApi())
    implementation(Dependencies.okhttp)
    implementation(Dependencies.okhttpLogging)
    implementation(Dependencies.gson)

    implementation(project(":common:okhttp"))
    implementation(project(":gradle:android"))
    implementation(project(":gradle:gradle-logger"))
    implementation(project(":gradle:git"))
    implementation(project(":gradle:gradle-extensions"))

    testImplementation(project(":common:test-okhttp"))
    testImplementation(project(":gradle:git-test-fixtures"))
    testImplementation(project(":gradle:test-project"))
}
