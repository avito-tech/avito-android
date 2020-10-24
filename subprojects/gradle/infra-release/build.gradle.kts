plugins {
    id("java-gradle-plugin")
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    implementation(project(":gradle:git"))
    implementation(project(":gradle:ci-logger"))
    implementation(Dependencies.funktionaleTry)

    testImplementation(project(":gradle:test-project"))
}

gradlePlugin {
    plugins {
        create("release") {
            id = "com.avito.android.infra-release"
            implementationClass = "com.avito.android.InfraReleasePlugin"
            displayName = "Infra Release"
        }
    }
}
