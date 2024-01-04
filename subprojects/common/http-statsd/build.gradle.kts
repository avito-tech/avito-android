plugins {
    id("convention.kotlin-jvm-android")
    id("convention.publish-kotlin-library")
}

dependencies {
    api(project(":subprojects:common:time"))
    api(project(":subprojects:common:statsd"))
    api(project(":subprojects:logger:logger"))
    api(project(":subprojects:common:okhttp"))

    testImplementation(project(":subprojects:common:test-okhttp"))
    testImplementation(project(":subprojects:common:truth-extensions"))
    testImplementation(project(":subprojects:common:junit-utils"))
    testImplementation(testFixtures(project(":subprojects:common:statsd")))
}
