package org.qmained.telegram.filter

import eu.vendeli.tgbot.TelegramBot
import eu.vendeli.tgbot.interfaces.helper.Filter
import eu.vendeli.tgbot.types.User
import eu.vendeli.tgbot.types.component.ProcessedUpdate
import jakarta.enterprise.context.ApplicationScoped
import org.qmained.configs.VkConfig

@ApplicationScoped
class AdminFilter(
    private val vkConfig: VkConfig,
) : Filter {
    override suspend fun match(
        user: User?,
        update: ProcessedUpdate,
        bot: TelegramBot,
    ): Boolean {
        return user?.id == vkConfig.tgAdminId()
    }
}
