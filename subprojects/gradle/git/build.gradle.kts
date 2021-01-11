plugins {
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    api(project(":gradle:build-environment")) {
        because("project.buildEnvironment only")
    }
    api(project(":gradle:process"))

    implementation(gradleApi())
    implementation(project(":common:logger"))
    implementation(project(":common:slf4j-logger"))
    implementation(project(":gradle:gradle-extensions"))
    implementation(Dependencies.funktionaleTry)

    testImplementation(project(":gradle:test-project"))
    testImplementation(project(":common:logger-test-fixtures"))
    testImplementation(Dependencies.Test.mockitoJUnitJupiter)
}
