plugins {
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    api(project(":gradle:build-environment")) // project.buildEnvironment only

    implementation(gradleApi())
    implementation(project(":gradle:kotlin-dsl-support"))
    implementation(project(":gradle:process"))
    implementation(Dependencies.funktionaleTry)

    testImplementation(project(":gradle:test-project"))
    testImplementation(Dependencies.Test.mockitoJUnitJupiter)
}
