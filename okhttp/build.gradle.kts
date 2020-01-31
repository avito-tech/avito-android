plugins {
    id("kotlin")
    `maven-publish`
}

val kotlinVersion: String by project
val funktionaleVersion: String by project
val okhttpVersion: String by project
val retrofitVersion: String by project

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    implementation("com.squareup.okhttp3:okhttp:$okhttpVersion")

    testImplementation(project(":test-okhttp"))
    testImplementation("org.funktionale:funktionale-try:$funktionaleVersion")
    testImplementation("com.squareup.okhttp3:mockwebserver:$okhttpVersion")
    testImplementation("com.squareup.retrofit2:retrofit:$retrofitVersion")
    testImplementation("com.squareup.retrofit2:converter-gson:$retrofitVersion")
}
