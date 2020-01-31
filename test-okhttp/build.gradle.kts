plugins {
    id("kotlin")
    `maven-publish`
}

val kotlinVersion: String by project
val okhttpVersion: String by project
val truthVersion: String by project

dependencies {
    api("com.squareup.okhttp3:mockwebserver:$okhttpVersion")
    implementation("com.google.truth:truth:$truthVersion")

    implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")

    testImplementation("io.kotlintest:kotlintest:2.0.7")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlinVersion")
}
