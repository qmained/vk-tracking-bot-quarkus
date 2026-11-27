package org.qmained.telegram.handlers

import eu.vendeli.tgbot.TelegramBot
import eu.vendeli.tgbot.annotations.CommonHandler
import eu.vendeli.tgbot.api.message.message
import eu.vendeli.tgbot.types.User
import eu.vendeli.tgbot.types.common.LinkPreviewOptions
import eu.vendeli.tgbot.types.component.MessageUpdate
import eu.vendeli.tgbot.types.component.ParseMode
import eu.vendeli.tgbot.utils.common.escapeMarkdownV2
import jakarta.enterprise.context.ApplicationScoped
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.qmained.model.ObservedUser
import org.qmained.service.UserService
import org.qmained.telegram.filter.AllowedUserFilter
import org.slf4j.LoggerFactory


@ApplicationScoped
class UserCommandHandler(
    private val userService: UserService,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @CommonHandler.Text(value = ["/add"], filters = [AllowedUserFilter::class])
    suspend fun addUser(user: User, bot: TelegramBot) {
        message { "Insert user's numeric id. It can be obtained " - textLink("https://regvk.com/id/") { "here" } }
            .options {
                linkPreviewOptions = LinkPreviewOptions(isDisabled = true)
            }
            .send(user, bot)
        bot.inputListener[user] = "addObservable"
        logger.info("[{}] {} {} typed /add", user.id, user.firstName, user.lastName)
    }

    @CommonHandler.Text(value = ["/delete"], filters = [AllowedUserFilter::class])
    suspend fun deleteUser(user: User, bot: TelegramBot) {
        message { "Insert user's id " }.send(user, bot)
        bot.inputListener[user] = "removeObservable"
        logger.info("[{}] {} {} typed /delete", user.id, user.firstName, user.lastName)
    }

    @CommonHandler.Text(value = ["/list"], filters = [AllowedUserFilter::class])
    suspend fun listUsers(user: User, bot: TelegramBot) {
        val observedUsers = withContext(Dispatchers.IO) {
            userService.getOrAddUser(user)
        }.observedUsers
        message {
            observedUsers.joinToString(separator = "\n", prefix = "User ids list:\n") { observedUser ->
                observedUser.toListLine()
            }
        }.options {
            parseMode = ParseMode.MarkdownV2
            linkPreviewOptions {
                isDisabled = true
            }
        }.send(user, bot)
        logger.info("[{}] {} {} typed /list", user.id, user.firstName, user.lastName)
    }

    private fun ObservedUser.toListLine(): String {
        val fullName = listOfNotNull(firstName, lastName)
            .joinToString(" ")
            .ifBlank { "[Blank]" }
            .escapeMarkdownV2()
        return "`${userId}`: [$fullName](https://vk.com/id${userId})"
    }

    @CommonHandler.Text(value = ["/start"], filters = [AllowedUserFilter::class])
    suspend fun start(user: User, bot: TelegramBot) {
        message { "VK tracker bot" }.send(user, bot)
        logger.info("[{}] {} {} typed /start", user.id, user.firstName, user.lastName)
    }

    @CommonHandler.Regex(value = ".*", filters = [AllowedUserFilter::class])
    suspend fun any(user: User, bot: TelegramBot, update: MessageUpdate?) {
        message { "Input command" }.send(user, bot)
        logger.info(
            "Not allowed user [{}] {} {} typed message: {}",
            user.id,
            user.firstName,
            user.lastName,
            update?.origin?.message?.text ?: ""
        )
    }

    @CommonHandler.Regex(value = ".*")
    fun anyUnallowed(user: User?, update: MessageUpdate?) {
        user?.run {
            logger.info(
                "[{}] {} {} typed message: {}",
                user.id,
                user.firstName,
                user.lastName,
                update?.origin?.message?.text ?: ""
            )
        }
    }

}
