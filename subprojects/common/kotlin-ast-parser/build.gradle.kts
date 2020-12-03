plugins {
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    implementation(project(":common:logger"))
    implementation(Dependencies.kotlinCompilerEmbeddable)
}
