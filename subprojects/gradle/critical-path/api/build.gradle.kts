plugins {
    id("convention.kotlin-jvm")
    id("convention.libraries")
    id("convention.publish-kotlin-library")
}

publish {
    artifactId.set("critical-path-api")
}

dependencies {
    api(project(":common:graph"))
    api(project(":common:result"))

    implementation(gradleApi())
    implementation(project(":gradle:gradle-extensions"))
    implementation(project(":gradle:gradle-profile"))
    implementation(project(":common:composite-exception"))
    implementation(project(":common:problem"))
    implementation(project(":logger:gradle-logger"))
}

kotlin {
    explicitApi()
}
