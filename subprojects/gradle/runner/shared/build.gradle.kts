plugins {
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

extra["artifact-id"] = "runner-shared"

dependencies {
    compileOnly(gradleApi())
    implementation(Dependencies.rxJava)
}
