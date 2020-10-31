plugins {
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    api(Dependencies.Gradle.androidPlugin)

    implementation(gradleApi())
    implementation(project(":gradle:files"))
    implementation(project(":gradle:process"))
    implementation(project(":gradle:kotlin-dsl-support"))
    implementation(Dependencies.funktionaleTry)

    testImplementation(project(":gradle:process-test-fixtures"))
}
