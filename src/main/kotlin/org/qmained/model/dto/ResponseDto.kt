package org.qmained.model.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import io.quarkus.runtime.annotations.RegisterForReflection

@JsonIgnoreProperties(ignoreUnknown = true)
@RegisterForReflection
data class ResponsesDto(
    @JsonProperty("response") var responses: List<ResponseDto>? = listOf(),
    var error: ErrorDto?
)

@JsonIgnoreProperties(ignoreUnknown = true)
@RegisterForReflection
data class ResponseDto(
    var id: Long = 0,
    var deactivated: String? = null,
    @JsonProperty("last_seen") var lastSeen: LastSeen?,
    var online: Int = 0,
    @JsonProperty("first_name") var firstName: String = "null",
    @JsonProperty("last_name") var lastName: String = "null",
    @JsonProperty("can_access_closed") var canAccessClosed: Boolean = false,
    @JsonProperty("is_closed") var isClosed: Boolean = false,
)

@RegisterForReflection
data class ErrorDto(
    @JsonProperty("error_code") var errorCode: Int,
    @JsonProperty(value = "error_msg") var errorMsg: String
)

@RegisterForReflection
data class LastSeen(
    var platform: Int = 0,
    var time: Long = 0,
)
