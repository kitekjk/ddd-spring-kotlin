plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":application"))

    implementation(libs.bundles.kotlin)
    implementation(libs.bundles.spring.boot.web)
    implementation(libs.jackson.module.kotlin)

    testImplementation(libs.spring.boot.starter.test)
}

tasks.bootJar {
    archiveFileName.set("app.jar")
}
