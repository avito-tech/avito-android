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

    implementation(project(":gradle:android"))
    implementation(project(":gradle:gradle-logger"))
    implementation(project(":gradle:git"))
    implementation(project(":gradle:gradle-extensions"))
    implementation(project(":gradle:process"))

    implementation(Dependencies.antPattern)
    implementation(Dependencies.Gradle.kotlinPlugin)

    testImplementation(project(":gradle:git-test-fixtures"))
    testImplementation(project(":gradle:test-project"))
    testImplementation(project(":common:logger-test-fixtures"))

    testImplementation(Dependencies.Test.mockitoKotlin)
}
