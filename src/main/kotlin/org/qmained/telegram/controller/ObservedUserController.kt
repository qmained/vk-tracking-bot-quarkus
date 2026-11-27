package org.qmained.telegram.controller

import eu.vendeli.tgbot.TelegramBot
import eu.vendeli.tgbot.annotations.InputHandler
import eu.vendeli.tgbot.api.message.message
import eu.vendeli.tgbot.types.User
import eu.vendeli.tgbot.types.component.MessageUpdate
import jakarta.enterprise.context.ApplicationScoped
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.qmained.configs.VkConfig
import org.qmained.service.ObservedUserService
import org.slf4j.LoggerFactory

@ApplicationScoped
class ObservedUserController(
    private val observedUserService: ObservedUserService,
    private val vkConfig: VkConfig,
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @InputHandler(["addObservable"])
    suspend fun addUser(update: MessageUpdate?, user: User, bot: TelegramBot) {
        logger.info(
            "[{}] {} {} on addObservable: {}",
            user.id,
            user.firstName,
            user.lastName,
            update?.origin?.message?.text ?: ""
        )
        if (update == null || update.origin.message?.text?.toLongOrNull() == null) {
            message { "Incorrect input, send again" }.send(user, bot)
            bot.inputListener[user] = "addObservable"
            return
        }

        update.origin.message?.text?.toLong()?.let { userId ->
            if (userId == vkConfig.vkAdminId() && user.id != vkConfig.tgAdminId() || userId < 0) {
                message { "This id cannot be added" }.send(user, bot)
                return
            }

            val isAdded = withContext(Dispatchers.IO) {
                observedUserService.addObservedUser(user, userId)
            }
            if (isAdded) {
                message { "Specified user id added" }.send(user, bot)
            } else {
                message { "Specified user is already added" }.send(user, bot)
            }

        }
    }

    @InputHandler(["removeObservable"])
    suspend fun removeUser(update: MessageUpdate?, user: User, bot: TelegramBot) {
        logger.info(
            "[{}] {} {} on removeObservable: {}",
            user.id,
            user.firstName,
            user.lastName,
            update?.origin?.message?.text ?: ""
        )

        if (update == null || update.origin.message?.text?.toLongOrNull() == null) {
            message { "Incorrect input, send again" }.send(user, bot)
            bot.inputListener[user] = "removeObservable"
            return
        }
        update.origin.message?.text?.let {
            val isRemoved = withContext(Dispatchers.IO) {
                observedUserService.removeObservedUser(user, it.toLong())
            }
            if (isRemoved) {
                message { "Specified user id removed" }.send(user, bot)
            } else {
                message { "This id is not in the list" }.send(user, bot)
            }
        }

    }

}
