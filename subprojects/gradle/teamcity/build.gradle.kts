plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
}

dependencies {
    api(project(":common:teamcity-common"))
    implementation(gradleApi())
    implementation(project(":gradle:gradle-extensions"))

    testImplementation(project(":gradle:test-project"))
}
