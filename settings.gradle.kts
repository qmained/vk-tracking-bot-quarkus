pluginManagement {
    val quarkusPluginVersion: String by settings
    val quarkusPluginId: String by settings
    repositories {
        mavenCentral()
        gradlePluginPortal()
        mavenLocal()
        maven {
            url = uri("http://192.168.1.142:8081")
            isAllowInsecureProtocol = true
            // Only KtGram artifacts live here; keeps an offline repo from breaking other resolution.
            content { includeGroupAndSubgroups("eu.vendeli") }
        }
    }
    plugins {
        id(quarkusPluginId) version quarkusPluginVersion
    }
}
rootProject.name = "vk-tracking-bot-quarkus"
