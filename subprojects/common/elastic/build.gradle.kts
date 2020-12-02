plugins {
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
    id("nebula.integtest")
}

dependencies {
    api(Dependencies.okhttp)

    implementation(project(":common:time"))

    implementation(Dependencies.gson)
    implementation(Dependencies.retrofit)
    implementation(Dependencies.retrofitConverterGson)

    testImplementation(project(":common:test-okhttp"))
    testImplementation(project(":common:time-test-fixtures"))
}
