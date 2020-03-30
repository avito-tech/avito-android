plugins {
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    implementation(Dependencies.bcel)
    implementation(Dependencies.gson)

    testImplementation(project(":subprojects:gradle:test-project"))
    testImplementation(Dependencies.kotlinPoet)
}
