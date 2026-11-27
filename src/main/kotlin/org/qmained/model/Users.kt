package org.qmained.model

import io.quarkus.hibernate.orm.panache.kotlin.PanacheCompanionBase
import io.quarkus.hibernate.orm.panache.kotlin.PanacheEntityBase
import jakarta.persistence.*

@Entity
class Users(
    @Id
    @Column(nullable = false, name = "user_id")
    var userId: Long,
    var firstName: String,
) : PanacheEntityBase {
    @ManyToMany(cascade = [CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH])
    @JoinTable(name = "users_observed_users")
    var observedUsers: MutableSet<ObservedUser> = mutableSetOf()

    var allowed: Boolean = false
    var lastName: String? = null
    var username: String? = null

    @Override
    override fun toString(): String {
        return this::class.simpleName + "(  userId = $userId )"
    }

    companion object : PanacheCompanionBase<Users, Long> {
        fun findUsersByUserId(userId: Long): Users? {
            return find(
                // language=HQL
                "FROM Users u LEFT JOIN FETCH u.observedUsers o WHERE u.userId = :userId ORDER BY o.userId",
                mapOf("userId" to userId)
            ).list().firstOrNull()
        }

        fun findAllWithObservedUsers(): List<Users> {
            return list(
                // language=HQL
                "FROM Users u LEFT JOIN FETCH u.observedUsers"
            )
        }

        fun findAllAllowed(): List<Users> {
            return list(
                // language=HQL
                "FROM Users u LEFT JOIN FETCH u.observedUsers WHERE u.allowed = true"
            )
        }
    }
}
