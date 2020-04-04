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

    implementation(project(":subprojects:gradle:utils"))
    implementation(project(":subprojects:gradle:logging"))
    implementation(project(":subprojects:gradle:android"))
    implementation(project(":subprojects:gradle:kotlin-dsl-support"))
    implementation(project(":subprojects:gradle:git"))

    testImplementation(project(":subprojects:gradle:test-project"))
    testImplementation(project(":subprojects:common:test-okhttp"))
    testImplementation(project(":subprojects:gradle:git-test-fixtures"))
}
