package org.qmained.service

import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.rest.client.inject.RestClient
import org.qmained.clients.VkClient
import org.qmained.configs.VkConfig
import org.qmained.model.dto.ResponsesDto
import org.slf4j.LoggerFactory

@ApplicationScoped
class VkService(
    @RestClient private val vkClient: VkClient,
    private val vkConfig: VkConfig,
) {
    private val logger = LoggerFactory.getLogger(VkService::class.java)

    suspend fun getUserStatuses(userIds: List<Long>): ResponsesDto {
        vkConfig.run {
            return vkClient.fetchUsers(
                accessToken = accessToken(),
                userIds = getUsersString(userIds),
                fields = fields(),
                v = v()
            ).also { logger.info("User statuses: {}", it) }
        }
    }

    private fun getUsersString(userIds: List<Long>): String {
        return userIds.joinToString(separator = ",")
    }

}
