plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.libraries")
}

dependencies {
    compileOnly(gradleApi())

    api(project(":subprojects:common:logger"))
    api(project(":subprojects:common:time"))

    implementation(project(":subprojects:gradle:sentry-config"))
    implementation(project(":subprojects:gradle:build-environment"))
    implementation(project(":subprojects:common:elastic-logger"))
    implementation(project(":subprojects:common:sentry-logger"))
    implementation(project(":subprojects:common:slf4j-logger"))

    testImplementation(libs.mockitoJUnitJupiter)
}
