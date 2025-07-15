plugins {
    `java-library`
    kotlin("jvm")
}

dependencies {
    implementation(libs.bundles.kotlin)

    testImplementation(libs.kotest.runner)
}
