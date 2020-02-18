plugins {
    id("kotlin")
    id("java-test-fixtures")
    `maven-publish`
    id("com.jfrog.bintray")
}

val retrofitVersion: String by project
val okhttpVersion: String by project

dependencies {
    implementation(project(":subprojects:common:time"))
    implementation(project(":subprojects:common:logger"))

    implementation("com.squareup.retrofit2:retrofit:$retrofitVersion")
    implementation("com.squareup.okhttp3:okhttp:$okhttpVersion")
}
