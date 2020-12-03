plugins {
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    implementation(gradleApi())
    implementation(project(":gradle:files"))
    implementation(project(":gradle:ci-logger"))
    implementation(project(":gradle:kotlin-dsl-support"))
    implementation(project(":gradle:instrumentation-tests-dex-loader")) {
        because("TestInApkModel")
    }
    implementation(project(":gradle:impact-shared")) {
        because("ChangesDetector reuse")
    }
}
