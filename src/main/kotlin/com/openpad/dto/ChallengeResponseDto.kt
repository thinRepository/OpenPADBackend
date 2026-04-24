package com.openpad.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Instant

/**
 * Data Transfer Object for challenge start response.
 *
 * **Why a separate DTO?**
 *  - Decouples the API contract from the entity structure.
 *  - If the entity schema changes (e.g., add internal fields), the API stays stable.
 *  - Clients never see database implementation details.
 *
 * Kotlin-for-Java-devs notes:
 *  - `data class` is a lightweight value object — common for DTOs.
 *  - No `@Entity` or database annotations; this is a pure data container.
 *  - Jackson (Spring's JSON mapper) automatically serializes data classes.
 */
data class ChallengeResponseDto @JsonCreator constructor(
    @JsonProperty("sha")
    val sha: String,
    @JsonProperty("createdAt")
    val createdAt: Instant,
    @JsonProperty("expiresAt")
    val expiresAt: Instant,
    @JsonProperty("ttlSeconds")
    val ttlSeconds: Long
)
