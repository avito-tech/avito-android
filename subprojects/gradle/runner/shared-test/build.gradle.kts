plugins {
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

extra["artifact-id"] = "runner-shared-test"

dependencies {
    api(Dependencies.Test.coroutinesTest)
    
    compileOnly(gradleApi())

    implementation(Dependencies.coroutinesCore)
    implementation(Dependencies.funktionaleTry)
    implementation(Dependencies.kotson)
    implementation(Dependencies.Test.junitJupiterApi)
    implementation(Dependencies.Test.truth)
    implementation(project(":common:report-viewer"))
    implementation(project(":gradle:runner:service"))
    implementation(project(":gradle:runner:shared"))
    implementation(project(":gradle:test-project"))
}
