plugins {
    id("kotlin")
}

dependencies {
    api(project(":common:time"))

    implementation(project(":common:logger-test-fixtures"))
}
