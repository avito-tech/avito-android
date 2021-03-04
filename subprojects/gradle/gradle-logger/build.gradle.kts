plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.libraries")
}

dependencies {
    compileOnly(gradleApi())

    api(project(":common:logger"))
    api(project(":common:time"))

    implementation(project(":gradle:sentry-config"))
    implementation(project(":gradle:build-environment"))
    implementation(project(":common:elastic-logger"))
    implementation(project(":common:sentry-logger"))
    implementation(project(":common:slf4j-logger"))

    testImplementation(libs.mockitoJUnitJupiter)
}
