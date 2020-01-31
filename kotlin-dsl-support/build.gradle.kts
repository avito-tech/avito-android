plugins {
    id("kotlin")
    `maven-publish`
}

dependencies {
    implementation(gradleApi())

    testImplementation(project(":test-project"))
}
