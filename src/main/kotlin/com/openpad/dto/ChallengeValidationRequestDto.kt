package com.openpad.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Data Transfer Object for challenge validation request.
 *
 * **Validation at API boundary:**
 *  - SHA is required (non-null)
 *  - SHA should be in valid format (will be validated in service)
 *
 * **Jackson Notes:**
 *  - `@JsonCreator` explicitly tells Jackson how to deserialize this class
 *  - `@JsonProperty` maps JSON field names to constructor parameters
 *  - This is necessary because Kotlin data classes don't have a no-arg constructor by default
 *
 * Kotlin-for-Java-devs notes:
 *  - No default values on constructor parameters = fields are required
 *  - Jackson automatically deserializes JSON to this data class
 */
data class ChallengeValidationRequestDto @JsonCreator constructor(
    @JsonProperty("sha")
    val sha: String
)
