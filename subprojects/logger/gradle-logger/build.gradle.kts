plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
}

dependencies {
    compileOnly(gradleApi())

    api(project(":logger:logger"))
    api(project(":common:time"))

    implementation(project(":gradle:sentry-config"))
    implementation(project(":gradle:build-environment"))
    implementation(project(":logger:elastic-logger"))
    implementation(project(":logger:sentry-logger"))
    implementation(project(":logger:slf4j-logger"))

    testImplementation(libs.mockitoJUnitJupiter)
}
