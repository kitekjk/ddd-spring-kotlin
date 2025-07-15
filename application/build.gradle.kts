plugins {
    kotlin("jvm")
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
    implementation(project(":infrastructure"))

    implementation(libs.bundles.kotlin)
    implementation("org.springframework:spring-context")
    implementation("org.springframework:spring-tx")

    testImplementation(libs.spring.boot.starter.test)
}
