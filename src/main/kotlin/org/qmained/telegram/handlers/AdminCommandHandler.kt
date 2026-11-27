package org.qmained.telegram.handlers

import eu.vendeli.tgbot.TelegramBot
import eu.vendeli.tgbot.annotations.CommonHandler
import eu.vendeli.tgbot.api.message.message
import eu.vendeli.tgbot.types.User
import eu.vendeli.tgbot.types.component.MessageUpdate
import jakarta.enterprise.context.ApplicationScoped
import org.qmained.telegram.filter.AdminFilter

@ApplicationScoped
class AdminCommandHandler {

    @CommonHandler.Text(["/allow"], filters = [AdminFilter::class])
    suspend fun allow(user: User, bot: TelegramBot) {
        message { "Enter id of a telegram user" }.send(user, bot)
        bot.inputListener[user] = "allowUser"
    }

    @CommonHandler.Text(["/forbid"], filters = [AdminFilter::class])
    suspend fun forbid(user: User, bot: TelegramBot) {
        message { "Enter id of a telegram user" }.send(user, bot)
        bot.inputListener[user] = "forbidUser"
    }

}
