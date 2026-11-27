package org.qmained.telegram.controller

import eu.vendeli.tgbot.TelegramBot
import eu.vendeli.tgbot.annotations.InputHandler
import eu.vendeli.tgbot.api.message.message
import eu.vendeli.tgbot.types.User
import eu.vendeli.tgbot.types.component.MessageUpdate
import jakarta.enterprise.context.ApplicationScoped
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.qmained.service.UserService

@ApplicationScoped
class AdminController(
    private val userService: UserService,
) {

    @InputHandler(["allowUser"])
    suspend fun allowUser(user: User, bot: TelegramBot, update: MessageUpdate?) {
        setAllowed(user, bot, update, allowed = true, successText = "is now whitelisted")
    }

    @InputHandler(["forbidUser"])
    suspend fun forbidUser(user: User, bot: TelegramBot, update: MessageUpdate?) {
        setAllowed(user, bot, update, allowed = false, successText = "removed from whitelist")
    }

    private suspend fun setAllowed(
        user: User,
        bot: TelegramBot,
        update: MessageUpdate?,
        allowed: Boolean,
        successText: String,
    ) {
        val userId = update?.message?.text?.toLongOrNull()
        val reply = when {
            userId == null -> "Incorrect input"
            withContext(Dispatchers.IO) {
                userService.setAllowed(userId, allowed)
            } -> "User $userId $successText"
            else -> "User $userId not found"
        }
        message { reply }.send(user, bot)
    }

}
