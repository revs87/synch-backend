plugins {
    id("synch.spring-boot-app")
    id("org.springframework.boot")
    kotlin("plugin.spring")
}

group = "com.rvcoding"
version = "0.0.1-SNAPSHOT"
description = "Synch Backend for users, notifications and chats"

dependencies {
    implementation(projects.common)
    implementation(projects.notification)
    implementation(projects.user)
    implementation(projects.chat)

    implementation(libs.spring.boot.starter.security)

    implementation(libs.spring.boot.starter.amqp)
    implementation(libs.spring.boot.starter.data.redis)
    implementation(libs.spring.boot.starter.data.jpa)
    runtimeOnly(libs.postgresql)
}
