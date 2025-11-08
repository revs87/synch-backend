import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    id("synch.spring-boot-app")
    id("org.springframework.boot")
    kotlin("plugin.spring")
}

group = "com.rvcoding"
version = "0.0.1-SNAPSHOT"
description = "Synch Backend for users, notifications and chats"

tasks {
    named<BootJar>("bootJar") {
        from(project(":notification").projectDir.resolve("src/main/resources")) {
            into("")
        }
        from(project(":user").projectDir.resolve("src/main/resources")) {
            into("")
        }
    }
}

dependencies {
    implementation(projects.common)
    implementation(projects.notification)
    implementation(projects.user)
    implementation(projects.chat)

    implementation(libs.jackson.module.datatype)
    implementation(libs.kotlin.reflect)
    implementation(libs.spring.boot.starter.security)

    implementation(libs.spring.boot.starter.mail)
    implementation(libs.spring.boot.starter.amqp)
    implementation(libs.spring.boot.starter.data.redis)
    implementation(libs.spring.boot.starter.data.jpa)
    runtimeOnly(libs.postgresql)
}
