plugins {
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    api(Dependencies.funktionaleTry)

    // todo used only for Commandline.translateCommandline(source)
    implementation(gradleApi())
    implementation(project(":subprojects:common:logger"))
}
