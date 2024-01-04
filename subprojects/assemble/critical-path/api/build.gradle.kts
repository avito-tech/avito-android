plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
}

publish {
    artifactId.set("critical-path-api")
}

dependencies {
    api(project(":subprojects:common:graph"))
    api(project(":subprojects:common:result"))

    implementation(gradleApi())
    implementation(project(":subprojects:gradle:gradle-extensions"))
    implementation(project(":subprojects:assemble:gradle-profile"))
    implementation(project(":subprojects:common:composite-exception"))
    implementation(project(":subprojects:common:problem"))
    implementation(project(":subprojects:logger:gradle-logger"))
}
