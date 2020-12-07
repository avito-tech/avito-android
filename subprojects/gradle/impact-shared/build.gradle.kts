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
    implementation(project(":gradle:ci-logger"))
    implementation(project(":gradle:git"))
    implementation(project(":gradle:kotlin-dsl-support"))
    implementation(project(":gradle:process"))

    implementation(Dependencies.antPattern)
    implementation(Dependencies.Gradle.kotlinPlugin)

    testImplementation(project(":gradle:git-test-fixtures"))
    testImplementation(project(":gradle:logging-test-fixtures"))
    testImplementation(project(":gradle:test-project"))

    testImplementation(Dependencies.Test.mockitoKotlin)
}
