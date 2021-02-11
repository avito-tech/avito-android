plugins {
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
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

    testImplementation(Dependencies.Test.junitJupiterApi)
    testImplementation(Dependencies.Test.mockitoJUnitJupiter)
    testImplementation(Dependencies.Test.truth)
}
