plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.test-fixtures")
}

dependencies {
    api(projects.subprojects.common.result)
    api(projects.subprojects.testRunner.testModel)
}
