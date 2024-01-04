plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.test-fixtures")
}

dependencies {
    api(project(":subprojects:common:result"))

    // todo used only for Commandline.translateCommandline(source)
    implementation(gradleApi())
    implementation(project(":subprojects:logger:logger"))
}
