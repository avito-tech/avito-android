plugins {
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

extra["artifact-id"] = "runner-shared-test"

dependencies {
    compileOnly(gradleApi())
    api(Dependencies.Test.coroutinesTest)
    implementation(Dependencies.coroutinesCore)
    implementation(Dependencies.Test.junitJupiterApi)
    implementation(Dependencies.funktionaleTry)
    implementation(Dependencies.Test.truth)
    implementation(project(":gradle:runner:service"))
    implementation(project(":gradle:runner:shared"))
    implementation(project(":gradle:test-project"))
}
