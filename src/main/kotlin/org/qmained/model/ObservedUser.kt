package org.qmained.model

import io.quarkus.hibernate.orm.panache.kotlin.PanacheCompanionBase
import io.quarkus.hibernate.orm.panache.kotlin.PanacheEntityBase
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.ManyToMany
import jakarta.persistence.Table

@Entity
@Table(name = "observed_user")
class ObservedUser(
    @Id
    @Column(name = "user_id", nullable = false)
    var userId: Long,
) : PanacheEntityBase {

    @Suppress("Unused")
    @ManyToMany(mappedBy = "observedUsers")
    var users: MutableSet<Users> = mutableSetOf()
    var deactivated: String? = null
    var firstName: String? = null
    var lastName: String? = null
    var online: Int? = null
    var lastSeen: Long? = null
    var platform: Int? = null

    @Override
    override fun toString(): String {
        return this::class.simpleName + "(  userId = $userId )"
    }

    companion object : PanacheCompanionBase<ObservedUser, Long> {
        fun findByUserId(userId: Long): ObservedUser? {
            return find(
                // language=HQL
                "FROM ObservedUser o LEFT JOIN FETCH o.users WHERE o.userId = :userId",
                mapOf("userId" to userId)
            ).list().firstOrNull()
        }

        fun findAllActiveObservedUsers(): List<ObservedUser> {
            return list(
                // language=HQL
                "FROM ObservedUser o JOIN FETCH o.users"
            )
        }
    }
}
