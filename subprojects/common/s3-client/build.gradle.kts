plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
}

dependencies {
    api(project(":subprojects:common:result"))
    implementation(libs.awsS3)
    testImplementation(libs.truth)
    testImplementation(libs.junitJupiterApi)
    testImplementation(libs.coroutinesTest)
    testImplementation(libs.mockitoKotlin)
    testRuntimeOnly(libs.junitJupiterEngine)
}
