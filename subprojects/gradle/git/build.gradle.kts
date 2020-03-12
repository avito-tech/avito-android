plugins {
    id("kotlin")
    id("java-test-fixtures")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    implementation(gradleApi())
    implementation(project(":subprojects:gradle:kotlin-dsl-support"))
    implementation(project(":subprojects:gradle:process"))
    implementation(project(":subprojects:gradle:utils")) // project.buildEnvironment only
    implementation(Dependencies.funktionaleTry)

    testImplementation(project(":subprojects:gradle:test-project"))
    testImplementation(Dependencies.test.mockitoJUnitJupiter)

    testFixturesImplementation(project(":subprojects:gradle:test-project"))
    testFixturesImplementation(Dependencies.kotlinStdlib)
}
