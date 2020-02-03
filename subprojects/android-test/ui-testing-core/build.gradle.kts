plugins {
    id("com.android.library")
    id("kotlin-android")
    id("digital.wup.android-maven-publish")
    `maven-publish`
}

val androidXTestVersion: String by project
val espressoVersion: String by project
val androidXVersion: String by project
val hamcrestVersion: String by project
val junitVersion: String by project

dependencies {
    api("androidx.test:core:$androidXTestVersion")
    api("androidx.test.espresso:espresso-core:$espressoVersion")
    api("androidx.test.espresso:espresso-web:$espressoVersion")
    api("androidx.test.espresso:espresso-intents:$espressoVersion")
    api("androidx.test.uiautomator:uiautomator:2.2.0")

    api("com.forkingcode.espresso.contrib:espresso-descendant-actions:1.4.0")

    api("androidx.appcompat:appcompat:$androidXVersion")
    api("androidx.recyclerview:recyclerview:$androidXVersion")
    api("com.google.android.material:material:$androidXVersion")

    implementation("org.hamcrest:hamcrest-library:$hamcrestVersion")
    implementation("junit:junit:$junitVersion")
    implementation("me.weishu:free_reflection:2.2.0")
}
