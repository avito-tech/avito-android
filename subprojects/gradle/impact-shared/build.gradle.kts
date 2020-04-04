plugins {
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    implementation(gradleApi())
    implementation(project(":subprojects:gradle:utils"))
    implementation(project(":subprojects:gradle:process"))
    implementation(project(":subprojects:gradle:logging"))
    implementation(project(":subprojects:gradle:android"))
    implementation(project(":subprojects:gradle:git"))
    implementation(project(":subprojects:gradle:kotlin-dsl-support"))
    implementation(Dependencies.antPattern)
    implementation(Dependencies.gradle.kotlinPlugin)
    implementation(Dependencies.funktionaleTry)

    testImplementation(project(":subprojects:gradle:test-project"))
    testImplementation(project(":subprojects:gradle:logging-test-fixtures"))
    testImplementation(project(":subprojects:gradle:git-test-fixtures"))
    testImplementation(Dependencies.test.mockitoKotlin)
}
