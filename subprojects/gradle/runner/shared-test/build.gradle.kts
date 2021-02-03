plugins {
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

extra["artifact-id"] = "runner-shared-test"

dependencies {
    api(project(":subprojects:gradle:runner:stub"))
    api(Dependencies.Test.coroutinesTest)

    compileOnly(gradleApi())

    implementation(Dependencies.coroutinesCore)
    implementation(Dependencies.funktionaleTry)
    implementation(Dependencies.kotson)
    implementation(Dependencies.Test.junitJupiterApi)
    implementation(Dependencies.Test.truth)
    implementation(project(":subprojects:common:report-viewer"))
    implementation(project(":subprojects:gradle:runner:service"))
    implementation(project(":subprojects:gradle:runner:shared"))
    implementation(project(":subprojects:gradle:test-project"))
}
