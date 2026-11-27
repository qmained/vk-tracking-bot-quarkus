package org.qmained.service

import eu.vendeli.tgbot.types.User
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.event.Event
import jakarta.transaction.Transactional
import org.qmained.events.NewUserEvent
import org.qmained.model.Users

/**
 * Every public method runs in a transaction (joining the caller's one if present).
 * Callers must be on a thread where blocking is allowed: bot handlers already run on
 * Dispatchers.IO; suspend code on the Vert.x event loop must use withContext(Dispatchers.IO).
 */
@ApplicationScoped
@Transactional
class UserService(
    private val newUserEvent: Event<NewUserEvent>,
) {

    fun getOrAddUser(user: User): Users {
        return getUser(user) ?: Users(user.id, user.firstName).apply {
            lastName = user.lastName
            username = user.username
            persist()
        }.also {
            newUserEvent.fire(
                NewUserEvent(
                    it.userId,
                    it.firstName,
                    it.lastName,
                    it.username,
                )
            )
        }
    }

    fun getAllAllowedUsers(): List<Users> = Users.findAllAllowed()

    fun getUser(user: User): Users? = Users.findUsersByUserId(user.id)

    fun getUser(userId: Long): Users? = Users.findUsersByUserId(userId)

    /** @return false if no such user exists */
    fun setAllowed(userId: Long, allowed: Boolean): Boolean {
        val user = Users.findById(userId) ?: return false
        user.allowed = allowed   // managed entity: written on commit
        return true
    }

    fun removeUser(user: User): Boolean {
        Users.findUsersByUserId(user.id)?.let {
            it.delete()
            return true
        }
        return false
    }

    fun getAllUsers(): List<Users> = Users.findAllWithObservedUsers()

}
