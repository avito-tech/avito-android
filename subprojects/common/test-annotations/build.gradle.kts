plugins {
    id("com.avito.android.kotlin-jvm")
    id("com.avito.android.publish-kotlin-library")
}

dependencies {
    implementation(project(":subprojects:common:report-viewer"))
}
