plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.libraries")
    id("convention.test-fixtures")
}

dependencies {
    api(project(":common:result"))

    // todo used only for Commandline.translateCommandline(source)
    implementation(gradleApi())
    implementation(project(":logger:logger"))
}
