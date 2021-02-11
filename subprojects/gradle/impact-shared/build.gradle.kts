plugins {
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    api(Dependencies.funktionaleTry) {
        because("Try<> is in ABI")
    }

    implementation(gradleApi())

    implementation(project(":subprojects:gradle:android"))
    implementation(project(":subprojects:gradle:gradle-logger"))
    implementation(project(":subprojects:gradle:git"))
    implementation(project(":subprojects:gradle:gradle-extensions"))
    implementation(project(":subprojects:gradle:process"))

    implementation(Dependencies.antPattern)
    implementation(Dependencies.Gradle.kotlinPlugin)

    testImplementation(project(":subprojects:gradle:git-test-fixtures"))
    testImplementation(project(":subprojects:gradle:test-project"))
    testImplementation(project(":subprojects:common:logger-test-fixtures"))

    testImplementation(Dependencies.Test.mockitoKotlin)
}
