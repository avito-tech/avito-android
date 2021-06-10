plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.test-fixtures")
    id("convention.libraries")
}

publish {
    artifactId.set("runner-service")
}

dependencies {
    api(project(":common:coroutines-extension"))
    api(project(":common:statsd"))
    api(project(":common:time"))
    api(project(":test-runner:command-line-executor"))

    implementation(project(":common:result"))
    implementation(project(":common:problem"))
    implementation(project(":test-runner:test-report-artifacts")) {
        because("DeviceWorker pulls test artifacts")
    }
    implementation(libs.ddmlib)
    implementation(libs.rxJava)
    implementation(libs.kotlinStdlibJdk7) {
        because("java.nio.file.Path extensions")
    }

    testImplementation(testFixtures(project(":logger:logger")))
    testImplementation(testFixtures(project(":common:time")))
    testImplementation(project(":common:files"))
    testImplementation(project(":common:truth-extensions"))
    testImplementation(project(":common:resources"))
    testImplementation(project(":gradle:test-project"))
    testImplementation(libs.kotlinReflect)
    testImplementation(libs.mockitoKotlin)
    testImplementation(libs.mockitoJUnitJupiter)
    testImplementation(libs.coroutinesTest)
}
