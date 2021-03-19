plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.libraries")
}

dependencies {
    api(project(":common:result"))

    // todo used only for Commandline.translateCommandline(source)
    implementation(gradleApi())
    implementation(project(":common:logger"))
}
