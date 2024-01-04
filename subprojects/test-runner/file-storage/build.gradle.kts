plugins {
    id("convention.kotlin-jvm-android")
    id("convention.publish-kotlin-library")
}

dependencies {
    api(project(":subprojects:common:result"))
    api(project(":subprojects:common:http-statsd"))
    implementation(libs.retrofit)
}
