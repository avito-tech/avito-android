plugins {
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    api(project(":common:teamcity-common"))
    implementation(gradleApi())
    implementation(project(":gradle:kotlin-dsl-support"))

    testImplementation(project(":gradle:test-project"))
}
