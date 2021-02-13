plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("com.avito.android.libraries")
}

dependencies {
    api(libs.funktionaleTry)

    // todo used only for Commandline.translateCommandline(source)
    implementation(gradleApi())
    implementation(project(":subprojects:common:logger"))
}
