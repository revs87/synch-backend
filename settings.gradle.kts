pluginManagement {
    includeBuild("build-logic")
    repositories {
        maven { url = uri("https://repo.spring.io/milestone") }
        maven { url = uri("https://repo.spring.io/snapshot") }
        gradlePluginPortal()
    }
}

/* Enables module lib access: implementation(projects.chat) */
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "synch"

include("app")
include("user")
include("chat")
include("notification")
include("common")