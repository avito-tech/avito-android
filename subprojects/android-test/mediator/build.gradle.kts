plugins {
    id("kotlin")
    `maven-publish`
}

dependencies {
    implementation(project(":subprojects:android-test:synth-test-module"))
}
