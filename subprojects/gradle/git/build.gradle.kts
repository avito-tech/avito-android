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
    implementation(Dependencies.funktionaleTry)
    api(project(":subprojects:gradle:build-environment")) // project.buildEnvironment only


    testImplementation(project(":subprojects:gradle:test-project"))
    testImplementation(Dependencies.test.mockitoJUnitJupiter)

    testFixturesImplementation(project(":subprojects:gradle:test-project"))
    testFixturesImplementation(Dependencies.kotlinStdlib)
}
