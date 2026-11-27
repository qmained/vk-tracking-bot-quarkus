package org.qmained.events

data class NewUserEvent(
    val userId: Long,
    val firstName: String,
    val lastName: String?,
    val username: String?,
)
