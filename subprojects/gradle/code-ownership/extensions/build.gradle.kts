plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
}

dependencies {
    api(projects.subprojects.gradle.codeOwnership.api)
    compileOnly(gradleApi())
    implementation(projects.subprojects.gradle.gradleExtensions)
}

publish {
    artifactId.set("code-ownership-extensions")
}
