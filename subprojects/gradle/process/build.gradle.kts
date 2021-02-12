plugins {
    id("com.avito.android.kotlin-jvm")
    id("com.avito.android.publish-kotlin-library")
    id("com.avito.android.libraries")
}

dependencies {
    api(libs.funktionaleTry)

    // todo used only for Commandline.translateCommandline(source)
    implementation(gradleApi())
    implementation(project(":subprojects:common:logger"))
}
