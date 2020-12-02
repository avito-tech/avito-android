plugins {
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
    id("nebula.integtest")
}

dependencies {
    implementation(project(":common:time"))

    implementation(Dependencies.gson)
    implementation(Dependencies.okhttp)
    implementation(Dependencies.okhttpLogging)
    implementation(Dependencies.retrofit)
    implementation(Dependencies.retrofitConverterScalars)

    testImplementation(project(":common:test-okhttp"))
    testImplementation(project(":common:time-test-fixtures"))
}
