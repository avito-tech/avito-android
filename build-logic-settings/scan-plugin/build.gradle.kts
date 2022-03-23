plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(project(":extensions"))
    implementation("com.gradle.enterprise:com.gradle.enterprise.gradle.plugin:3.7.2")

// Uncomment to reproduce the error
//    implementation("com.avito.android:build-trace:local")
}
