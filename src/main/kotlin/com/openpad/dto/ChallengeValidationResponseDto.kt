package com.openpad.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Data Transfer Object for challenge validation response.
 *
 * **Response fields:**
 *  - [isValid]: true if challenge passed validation, false otherwise
 *  - [message]: Human-readable message explaining the validation result
 *
 * Kotlin-for-Java-devs notes:
 *  - `data class` automatically implements equals(), hashCode(), toString()
 *  - Jackson serializes this to JSON automatically
 */
data class ChallengeValidationResponseDto @JsonCreator constructor(
    @JsonProperty("isValid")
    val isValid: Boolean,
    @JsonProperty("message")
    val message: String
)
