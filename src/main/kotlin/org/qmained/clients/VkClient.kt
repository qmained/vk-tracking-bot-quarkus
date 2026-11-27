package org.qmained.clients

import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.QueryParam
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient
import org.qmained.model.dto.ResponsesDto


@RegisterRestClient(configKey = "vk-api")
@Path("/method")
interface VkClient {
    @GET
    @Path("users.get")
    suspend fun fetchUsers(
        @QueryParam(value = "access_token") accessToken: String,
        @QueryParam("user_ids") userIds: String,
        @QueryParam("fields") fields: String,
        @QueryParam("v") v: String,
    ): ResponsesDto
}
