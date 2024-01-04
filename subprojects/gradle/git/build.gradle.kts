plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.test-fixtures")
}

dependencies {
    api(project(":subprojects:gradle:process"))
    api(project(":subprojects:common:result"))

    implementation(gradleApi())
    implementation(project(":subprojects:gradle:gradle-extensions"))

    testImplementation(libs.mockitoJUnitJupiter)
    testImplementation(project(":subprojects:gradle:test-project"))
    testImplementation(project(":subprojects:logger:logger"))
}
