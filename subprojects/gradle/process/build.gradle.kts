plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.test-fixtures")
}

dependencies {
    api(project(":common:result"))

    // todo used only for Commandline.translateCommandline(source)
    implementation(gradleApi())
    implementation(project(":common:logger"))
    implementation(libs.kotlinStdlib)
}
