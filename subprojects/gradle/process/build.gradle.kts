plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.libraries")
}

dependencies {
    api(libs.funktionaleTry)

    // todo used only for Commandline.translateCommandline(source)
    implementation(gradleApi())
    implementation(project(":common:logger"))
}
