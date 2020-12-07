plugins {
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    implementation(Dependencies.bcel)
    implementation(Dependencies.gson)

    testImplementation(project(":gradle:test-project"))
    testImplementation(project(":common:resources"))
}
