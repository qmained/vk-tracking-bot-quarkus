package org.qmained.configs

import io.smallrye.config.ConfigMapping
import io.smallrye.config.WithDefault

@ConfigMapping(prefix = "vk")
interface VkConfig {
    fun accessToken(): String

    @WithDefault("last_seen,online")
    fun fields(): String

    fun v(): String

    fun tgAdminId(): Long

    fun vkAdminId(): Long
}
