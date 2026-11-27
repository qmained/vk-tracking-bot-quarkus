package org.qmained.telegram.filter

import eu.vendeli.tgbot.TelegramBot
import eu.vendeli.tgbot.interfaces.helper.Filter
import eu.vendeli.tgbot.types.User
import eu.vendeli.tgbot.types.component.ProcessedUpdate
import jakarta.enterprise.context.ApplicationScoped
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.qmained.service.UserService

@ApplicationScoped
class AllowedUserFilter(
    private val userService: UserService,
) : Filter {
    override suspend fun match(user: User?, update: ProcessedUpdate, bot: TelegramBot): Boolean {
        return user?.let {
            withContext(Dispatchers.IO) {
                userService.getOrAddUser(user)
            }.allowed
        } ?: false
    }
}
