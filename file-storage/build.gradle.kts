plugins {
    id("kotlin")
    id("java-test-fixtures")
    `maven-publish`
}

val retrofitVersion: String by project
val okhttpVersion: String by project

dependencies {
    implementation(gradleApi())

    implementation(project(":kotlin-dsl-support"))
    implementation(project(":utils"))
    implementation(project(":time"))

    implementation("com.squareup.retrofit2:retrofit:$retrofitVersion")
    implementation("com.squareup.retrofit2:converter-gson:$retrofitVersion")
    implementation("com.squareup.okhttp3:okhttp:$okhttpVersion")
}
