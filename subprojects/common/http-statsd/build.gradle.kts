plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
}

dependencies {
    api(project(":subprojects:common:time"))
    api(project(":subprojects:common:statsd"))
    api(project(":subprojects:common:graphite"))
    api(project(":subprojects:logger:logger"))
    api(project(":subprojects:common:okhttp"))

    testImplementation(project(":subprojects:common:test-okhttp"))
    testImplementation(project(":subprojects:common:truth-extensions"))
    testImplementation(project(":subprojects:common:junit-utils"))
    testImplementation(testFixtures(project(":subprojects:common:statsd")))
}
