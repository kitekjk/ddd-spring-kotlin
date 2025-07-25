plugins {
    kotlin("jvm")
    kotlin("plugin.jpa")
    kotlin("plugin.spring")
    id("org.springframework.boot") apply false
    id("io.spring.dependency-management")
}

dependencyManagement {
    imports {
        mavenBom(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
    }
}

dependencies {
    implementation(project(":domain"))

    implementation(libs.bundles.kotlin)
    implementation(libs.spring.boot.starter.data.jpa)

    runtimeOnly(libs.h2)

    testImplementation(libs.spring.boot.starter.test) {
        exclude(module = "mockito-core")
    }
    testImplementation(libs.kotest.runner)
    testImplementation(libs.kotest.spring)
    testImplementation(libs.springmockk)
}
