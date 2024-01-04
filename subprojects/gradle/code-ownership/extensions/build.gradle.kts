plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
}

dependencies {
    api(project(":subprojects:gradle:code-ownership:api"))
    compileOnly(gradleApi())
    implementation(project(":subprojects:gradle:gradle-extensions"))
}

publish {
    artifactId.set("code-ownership-extensions")
}
