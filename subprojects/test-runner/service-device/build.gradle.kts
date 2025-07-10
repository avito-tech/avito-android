plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.test-fixtures")
}

publish {
    artifactId.set("runner-service-device")
}

dependencies {
    api(project(":subprojects:common:statsd"))
    api(project(":subprojects:test-runner:runner-api"))
    api(project(":subprojects:test-runner:device-provider:model"))
    api(project(":subprojects:logger:logger"))
    api(project(":subprojects:common:time"))
    api(project(":subprojects:common:result"))

    implementation(project(":subprojects:common:command-line-rx"))
    implementation(project(":subprojects:common:problem"))
    implementation(project(":subprojects:common:retry-action"))

    implementation(libs.ddmlib)

    testImplementation(project(":subprojects:common:resources"))
    testImplementation(project(":subprojects:common:truth-extensions"))
}
