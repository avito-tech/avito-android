plugins {
    id("kotlin")
    id("java-test-fixtures")
    `maven-publish`
}

val retrofitVersion: String by project
val okhttpVersion: String by project

dependencies {
    implementation(gradleApi())

    implementation(project(":subprojects:gradle:kotlin-dsl-support"))
    implementation(project(":subprojects:gradle:utils"))
    implementation(project(":subprojects:common:time"))

    implementation("com.squareup.retrofit2:retrofit:$retrofitVersion")
    implementation("com.squareup.retrofit2:converter-gson:$retrofitVersion")
    implementation("com.squareup.okhttp3:okhttp:$okhttpVersion")
}
