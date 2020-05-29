plugins {
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    implementation(gradleApi())
    implementation(project(":gradle:kotlin-dsl-support"))
    implementation(project(":gradle:process"))
    implementation(Dependencies.funktionaleTry)
    api(project(":gradle:build-environment")) // project.buildEnvironment only


    testImplementation(project(":gradle:test-project"))
    testImplementation(Dependencies.test.mockitoJUnitJupiter)
}
