plugins {
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    implementation(project(":gradle:kotlin-dsl-support"))
    implementation(project(":gradle:utils"))
    implementation(project(":gradle:logging"))
    implementation(gradleApi())
    implementation(Dependencies.googlePublish)
}
