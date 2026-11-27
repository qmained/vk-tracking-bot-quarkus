package org.qmained.telegram.utils

import eu.vendeli.tgbot.api.message.SendMessageAction
import eu.vendeli.tgbot.api.message.message
import eu.vendeli.tgbot.types.component.ParseMode
import eu.vendeli.tgbot.utils.common.escapeMarkdownV2
import org.qmained.model.ObservedUser
import org.qmained.model.dto.ResponseDto
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

private val pattern: DateTimeFormatter =
    DateTimeFormatter.ofPattern("MMMM dd: HH:mm:ss").withZone(ZoneId.systemDefault())

fun generateStatusMessage(currentObservedUser: ResponseDto, previousObservedUser: ObservedUser): SendMessageAction {
    val id = currentObservedUser.id
    val online = currentObservedUser.online
    val lastOnline = previousObservedUser.online
    val deactivated = currentObservedUser.deactivated
    val firstName = currentObservedUser.firstName
    val lastName = currentObservedUser.lastName
    val lastSeen = currentObservedUser.lastSeen
    val dateTimeLastSeen = if (lastSeen != null) pattern.format(
        LocalDateTime.ofEpochSecond(
            lastSeen.time,
            0,
            ZoneOffset.ofHours(3)
        )
    ) else "?"
    val timeDifference = if (previousObservedUser.lastSeen != null && lastSeen != null) {
        getTimeDifference(lastSeen.time - previousObservedUser.lastSeen!!)
    } else null

    val platform = when (currentObservedUser.lastSeen?.platform) {
        1 -> "Mobile version"
        2 -> "iPhone"
        3 -> "iPad"
        4 -> "Android"
        5 -> "Windows Phone"
        6 -> "Windows 10"
        7 -> "PC"
        else -> "?"
    }

    return message {
        """
        |Name: [${firstName.escapeMarkdownV2()} ${lastName.escapeMarkdownV2()}](https://vk.com/id$id)${deactivated?.let { "\n|$it" } ?: ""}
        |online: ${if (online == 0) "no" else "yes"}
        |last seen: $dateTimeLastSeen
        |was ${if (lastOnline == 0) "offline" else "online"} for: ${timeDifference ?: "?"}
        |from: $platform
        """.trimMargin()
    }.options {
        parseMode = ParseMode.MarkdownV2
        linkPreviewOptions {
            isDisabled = true
        }
    }
}

private fun getTimeDifference(difference: Long): String {
    val duration = Duration.ofSeconds(difference)
    val result = String.format(
        "%dd %02d:%02d:%02d",
        duration.toDaysPart(),
        duration.toHoursPart(),
        duration.toMinutesPart(),
        duration.toSecondsPart()
    )
    return result
}
