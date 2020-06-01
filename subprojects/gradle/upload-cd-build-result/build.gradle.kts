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

    implementation(project(":gradle:utils"))
    implementation(project(":gradle:logging"))
    implementation(project(":gradle:android"))
    implementation(project(":gradle:kotlin-dsl-support"))
    implementation(project(":gradle:git"))

    testImplementation(project(":gradle:test-project"))
    testImplementation(project(":common:test-okhttp"))
    testImplementation(project(":gradle:git-test-fixtures"))
}
