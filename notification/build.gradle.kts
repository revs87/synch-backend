plugins {
    id("java-library")
    id("synch.spring-boot-service")
    kotlin("plugin.jpa")
}

group = "com.rvcoding"
version = "unspecified"

repositories {
    mavenCentral()
    maven { url = uri("https://repo.spring.io/milestone") }
    maven { url = uri("https://repo.spring.io/snapshot") }
}

dependencies {
    implementation(projects.common)

    implementation(libs.spring.boot.starter.mail)
    implementation(libs.spring.boot.starter.amqp)
    implementation(libs.spring.boot.starter.thymeleaf)
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}