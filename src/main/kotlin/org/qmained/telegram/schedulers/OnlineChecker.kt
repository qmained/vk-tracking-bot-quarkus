package org.qmained.telegram.schedulers

import eu.vendeli.tgbot.TelegramBot
import io.quarkus.scheduler.Scheduled
import jakarta.enterprise.context.ApplicationScoped
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.qmained.model.ObservedUser
import org.qmained.service.ObservedUserService
import org.qmained.service.VkService
import org.qmained.telegram.utils.generateStatusMessage

private const val FLOOD_ERROR_CODE = 9

@ApplicationScoped
class OnlineChecker(
    private val vkService: VkService,
    private val bot: TelegramBot,
    private val observedUserService: ObservedUserService,
) {
    private var pauseTime: Int = 0

    @Scheduled(every = "4s", concurrentExecution = Scheduled.ConcurrentExecution.SKIP)
    suspend fun sendOnlineStatus() {
        if (pauseTime <= 0) {
            checkUsers(withContext(Dispatchers.IO) {
                observedUserService.getAllActiveObservedUsers()
            })
        } else {
            pauseTime--
        }
    }


    private suspend fun checkUsers(
        observedUsers: List<ObservedUser>,
    ) = coroutineScope {
        val userStatuses = vkService.getUserStatuses(observedUsers.map { it.userId })
        userStatuses.error?.let {
            if (it.errorCode == FLOOD_ERROR_CODE) {
                pauseTime = 15
                return@coroutineScope
            }
        }
        val byId = observedUsers.associateBy { it.userId }
        val changed = userStatuses.responses.orEmpty().filter { userStatus ->
            val observedUser = byId[userStatus.id] ?: return@filter false
            (userStatus.lastSeen != null || userStatus.deactivated != null)
                && (observedUser.online != userStatus.online || userStatus.lastSeen?.platform != observedUser.platform)
        }
        if (changed.isEmpty()) return@coroutineScope

        changed.forEach { userStatus ->
            val observedUser = byId.getValue(userStatus.id)
            observedUser.users.forEach { user ->     // fetched by JOIN FETCH, safe outside the transaction
                launch {
                    generateStatusMessage(userStatus, observedUser).send(user.userId, bot)
                }
            }
        }

        // Hibernate ORM is blocking: run the transaction off the Vert.x event loop
        withContext(Dispatchers.IO) {
            observedUserService.updateObservedUsers(changed)
        }
    }
}
