plugins {
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    implementation(project(":gradle:kotlin-dsl-support"))
    implementation(project(":common:report-viewer"))
    implementation(project(":common:statsd"))
    implementation(project(":gradle:utils"))
    implementation(project(":gradle:ci-logger"))
    implementation(project(":gradle:slack"))
    implementation(project(":gradle:bitbucket"))
    implementation(project(":gradle:statsd-config"))
    implementation(Dependencies.coroutinesCore)
    implementation(Dependencies.funktionaleTry)
    implementation(Dependencies.okhttp)

    testImplementation(project(":gradle:test-project"))
    testImplementation(project(":common:report-viewer-test-fixtures"))
}
