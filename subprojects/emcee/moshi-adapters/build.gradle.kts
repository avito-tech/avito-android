plugins {
    id("convention.kotlin-jvm")
    id("convention.unit-testing")
    id("convention.publish-kotlin-library")
}

dependencies {
    implementation(libs.moshiAdapters)
    testImplementation(libs.moshiKotlin)
    testImplementation(libs.junitJupiterParams)
}
