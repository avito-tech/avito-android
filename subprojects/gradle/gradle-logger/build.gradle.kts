plugins {
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    compileOnly(gradleApi())

    api(project(":common:logger"))
    api(project(":common:time"))

    implementation(project(":gradle:sentry-config"))
    implementation(project(":gradle:build-environment"))
    implementation(project(":common:elastic"))
    implementation(project(":common:slf4j-logger"))

    testImplementation(Dependencies.Test.junitJupiterApi)
    testImplementation(Dependencies.Test.mockitoJUnitJupiter)
    testImplementation(Dependencies.Test.truth)
}
