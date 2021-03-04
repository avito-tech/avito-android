plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.libraries")
}

publish {
    artifactId.set("runner-shared")
}

dependencies {
    compileOnly(gradleApi())
    api(project(":common:logger"))
    implementation(libs.rxJava)
}
