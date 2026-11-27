package org.qmained.notifications

import eu.vendeli.tgbot.TelegramBot
import eu.vendeli.tgbot.api.message.message
import eu.vendeli.tgbot.types.component.ParseMode
import eu.vendeli.tgbot.utils.common.escapeMarkdownV2
import io.quarkus.runtime.ShutdownEvent
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.event.Observes
import jakarta.enterprise.event.TransactionPhase
import kotlinx.coroutines.*
import org.qmained.configs.VkConfig
import org.qmained.events.NewUserEvent

@ApplicationScoped
class AdminNotifier(
    private val bot: TelegramBot,
    private val vkConfig: VkConfig,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun onNewUser(@Observes(during = TransactionPhase.AFTER_SUCCESS) e: NewUserEvent) {
        scope.launch {
            message {
                """
                |New user appeared:
                |id: `${e.userId}`
                |firstName: [${e.firstName.escapeMarkdownV2()}](tg://user?id=${e.userId})
                |lastName: ${e.lastName?.escapeMarkdownV2()}
                |username: ${e.username?.escapeMarkdownV2()}
                """.trimMargin()
            }.options { parseMode = ParseMode.MarkdownV2 }
                .send(vkConfig.tgAdminId(), bot)
        }
    }

    fun onStop(@Observes ev: ShutdownEvent) {
        scope.cancel()
    }
}
