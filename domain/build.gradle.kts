plugins {
    `java-library`
    kotlin("jvm")
    alias(libs.plugins.kotest)
}

dependencies {
    implementation(libs.bundles.kotlin)

    testImplementation(libs.bundles.junit.test)
    testImplementation(libs.bundles.kotest)
}

tasks.withType<Test> {
    useJUnitPlatform()

    // Configure Kotest
    systemProperty("kotest.framework.classpath.scanning.config.disable", "false")
    systemProperty("kotest.framework.classpath.scanning.autoscan.disable", "false")
}
