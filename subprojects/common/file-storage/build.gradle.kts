plugins {
    id("kotlin")
    id("java-test-fixtures")
    `maven-publish`
}

val retrofitVersion: String by project
val okhttpVersion: String by project

dependencies {
    implementation(project(":subprojects:common:time"))

    implementation("com.squareup.retrofit2:retrofit:$retrofitVersion")
    implementation("com.squareup.okhttp3:okhttp:$okhttpVersion")
}
