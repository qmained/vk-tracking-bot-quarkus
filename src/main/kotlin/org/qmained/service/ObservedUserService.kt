package org.qmained.service

import eu.vendeli.tgbot.types.User
import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import org.qmained.model.ObservedUser
import org.qmained.model.dto.ResponseDto
import org.slf4j.LoggerFactory

/**
 * Each public method is one unit of work: load, change and finish inside one transaction.
 * Entities returned from here are detached; only read fields (and collections fetched by the query).
 */
@ApplicationScoped
@Transactional
class ObservedUserService(
    private val userService: UserService,
) {
    private val logger = LoggerFactory.getLogger(ObservedUserService::class.java)

    /** @return false if [user] already observes [userId] */
    fun addObservedUser(user: User, userId: Long): Boolean {
        val owner = userService.getOrAddUser(user)
        if (owner.observedUsers.any { it.userId == userId }) return false

        val observed = ObservedUser.findById(userId)
            ?: ObservedUser(userId).also { it.persist() }
        owner.observedUsers.add(observed)
        observed.users.add(owner)
        return true
    }

    /** @return false if [user] didn't observe [userId] */
    fun removeObservedUser(user: User, userId: Long): Boolean {
        val owner = userService.getUser(user) ?: return false
        return owner.observedUsers.removeIf { it.userId == userId }
    }

    fun getAll(): List<ObservedUser> = ObservedUser.listAll()

    fun getAllActiveObservedUsers(): List<ObservedUser> = ObservedUser.findAllActiveObservedUsers()

    /** Applies fresh VK statuses to the stored users in one transaction. */
    fun updateObservedUsers(statuses: List<ResponseDto>) {
        if (statuses.isEmpty()) return
        val byId = ObservedUser.list("userId in ?1", statuses.map { it.id }).associateBy { it.userId }
        statuses.forEach { s ->
            byId[s.id]?.apply {
                firstName = s.firstName
                lastName = s.lastName
                deactivated = s.deactivated
                online = s.online
                lastSeen = s.lastSeen?.time
                platform = s.lastSeen?.platform
                logger.info("Updated user: {}", userId)
            }
        }
    }
}
