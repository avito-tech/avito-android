plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.test-fixtures")
}

publish {
    artifactId.set("runner-service")
}

dependencies {
    api(project(":subprojects:gradle:process"))
    api(project(":subprojects:test-runner:device-provider:api"))
    api(project(":subprojects:test-runner:service-device"))
    api(project(":subprojects:test-runner:test-model"))
    api(project(":subprojects:test-runner:runner-api"))

    implementation(project(":subprojects:common:coroutines-extension"))
    implementation(project(":subprojects:common:command-line-rx"))
    implementation(project(":subprojects:common:retry-action"))
    implementation(project(":subprojects:common:result"))
    implementation(project(":subprojects:test-runner:test-report-artifacts")) {
        because("DeviceWorker pulls test artifacts")
    }
    implementation(libs.ddmlib)

    testImplementation(libs.coroutinesTest)
    testImplementation(project(":subprojects:common:files"))
    testImplementation(project(":subprojects:common:truth-extensions"))
    testImplementation(testFixtures(project(":subprojects:test-runner:device-provider:model")))
}
