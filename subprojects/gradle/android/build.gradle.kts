plugins {
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    api(Dependencies.gradle.androidPlugin)

    implementation(gradleApi())
    implementation(project(":subprojects:gradle:files"))
    implementation(project(":subprojects:gradle:process"))
    implementation(project(":subprojects:gradle:kotlin-dsl-support"))
    implementation(Dependencies.funktionaleTry)

    testImplementation(project(":subprojects:gradle:process-test-fixtures"))
    testImplementation(project(":subprojects:gradle:test-project"))
}
