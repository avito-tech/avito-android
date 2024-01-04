plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.test-fixtures")
}

publish {
    artifactId.set("runner-service")
}

dependencies {
    api(project(":subprojects:common:coroutines-extension"))
    api(project(":subprojects:common:statsd"))
    api(project(":subprojects:common:time"))
    api(project(":subprojects:gradle:process"))
    api(project(":subprojects:common:command-line-rx"))
    api(project(":subprojects:test-runner:device-provider:api"))
    api(project(":subprojects:test-runner:test-model"))
    api(project(":subprojects:test-runner:runner-api"))

    implementation(project(":subprojects:common:result"))
    implementation(project(":subprojects:common:problem"))
    implementation(project(":subprojects:test-runner:test-report-artifacts")) {
        because("DeviceWorker pulls test artifacts")
    }
    implementation(libs.ddmlib)
    implementation(libs.rxJava)
    implementation(libs.kotlinStdlib) {
        because("java.nio.file.Path extensions")
    }

    testImplementation(libs.coroutinesTest)
    testImplementation(project(":subprojects:common:files"))
    testImplementation(project(":subprojects:common:resources"))
    testImplementation(project(":subprojects:common:truth-extensions"))
    testImplementation(project(":subprojects:logger:logger"))
    testImplementation(testFixtures(project(":subprojects:common:time")))
    testImplementation(testFixtures(project(":subprojects:test-runner:device-provider:model")))
}
