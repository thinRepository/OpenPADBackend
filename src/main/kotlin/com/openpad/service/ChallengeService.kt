package com.openpad.service

import com.openpad.dto.ChallengeResponseDto
import com.openpad.dto.ChallengeValidationRequestDto
import com.openpad.dto.ChallengeValidationResponseDto
import com.openpad.entity.Challenge
import com.openpad.repository.ChallengeRepository
import org.springframework.stereotype.Service
import java.security.MessageDigest
import java.time.Instant
import java.util.UUID

/**
 * Service encapsulating challenge creation business logic.
 *
 * **Responsibilities:**
 *  - Generate unique SHA256 hashes for challenges.
 *  - Set challenge expiration (TTL).
 *  - Persist challenges to the repository.
 *  - Convert entities to DTOs for API responses.
 *
 * **Why inject the repository?**
 *  - The service doesn't know or care about the database type (H2, PostgreSQL, MSSQL).
 *  - When you switch databases, the service is unaware — only config changes.
 *
 * Kotlin-for-Java-devs notes:
 *  - Constructor injection is idiomatic; declare dependencies in the class signature.
 *  - `private val` makes the field final and immutable.
 */
@Service
class ChallengeService(private val challengeRepository: ChallengeRepository) {

    companion object {
        private const val TTL_SECONDS = 300L  // 5 minutes
    }

    /**
     * Start a new challenge session.
     *
     * This generates a unique SHA256 hash, records the current timestamp,
     * calculates the expiration time (TTL), and persists to the database.
     *
     * **Steps:**
     *  1. Generate a unique SHA256 hash (from UUID + current millis).
     *  2. Set createdAt to now.
     *  3. Set expiresAt to now + TTL_SECONDS.
     *  4. Persist to database via repository.
     *  5. Return as DTO to the controller.
     *
     * @return [ChallengeResponseDto] with SHA, timestamps, and TTL
     */
    fun startChallenge(): ChallengeResponseDto {
        val now = Instant.now()
        val expiresAt = now.plusSeconds(TTL_SECONDS)

        // Generate unique SHA256 hash from UUID + nanoTime for uniqueness
        val sha = generateSha(UUID.randomUUID().toString() + System.nanoTime())

        // Create and persist the challenge
        val challenge = Challenge(
            sha = sha,
            createdAt = now,
            expiresAt = expiresAt
        )

        challengeRepository.save(challenge)

        // Convert to DTO for API response
        return ChallengeResponseDto(
            sha = challenge.sha,
            createdAt = challenge.createdAt,
            expiresAt = challenge.expiresAt,
            ttlSeconds = TTL_SECONDS
        )
    }

    /**
     * Validate a challenge using SHA and creation timestamp.
     *
     * **Validation steps:**
     *  1. Validate input fields (SHA and createdAt) are present and properly formatted
     *  2. Look up the challenge by SHA in the database
     *  3. Verify the createdAt timestamp matches the one in the database
     *  4. Check if the challenge has expired (current time > expiresAt)
     *  5. Return validation result
     *
     * **Error cases:**
     *  - Missing or empty SHA → "SHA is required and cannot be empty"
     *  - Missing or null createdAt → "Creation timestamp is required"
     *  - SHA not found in DB → "Challenge not found"
     *  - createdAt doesn't match DB → "Challenge creation timestamp mismatch"
     *  - Challenge expired → "Challenge has expired"
     *  - Valid challenge → "Challenge is valid"
     *
     * @param request [ChallengeValidationRequestDto] with SHA and createdAt
     * @return [ChallengeValidationResponseDto] with validation result
     */
    fun validateChallenge(request: ChallengeValidationRequestDto): ChallengeValidationResponseDto {
        // Step 1: Validate input fields before any database operations
        val shaValidation = validateShaFormat(request.sha)
        if (!shaValidation.first) {
            return ChallengeValidationResponseDto(
                isValid = false,
                message = shaValidation.second
            )
        }

        // Step 2: Look up the challenge by SHA in the database
        val challenge = challengeRepository.findBySha(request.sha)
        if (challenge == null) {
            return ChallengeValidationResponseDto(
                isValid = false,
                message = "Challenge not found"
            )
        }

        // Step 3: Verify createdAt matches the database record
        if (challenge.createdAt != request.createdAt) {
            return ChallengeValidationResponseDto(
                isValid = false,
                message = "Challenge creation timestamp mismatch"
            )
        }

        // Step 4: Check if the challenge has expired
        val now = Instant.now()
        if (now.isAfter(challenge.expiresAt)) {
            return ChallengeValidationResponseDto(
                isValid = false,
                message = "Challenge has expired"
            )
        }

        // Step 5: Challenge is valid
        return ChallengeValidationResponseDto(
            isValid = true,
            message = "Challenge is valid"
        )
    }

    /**
     * Validate SHA format before database lookup.
     *
     * **Format requirements:**
     *  - Must be 64 characters long (SHA256 in hex)
     *  - Must contain only hexadecimal characters (0-9, a-f)
     *
     * @param sha the SHA to validate
     * @return Pair of (isValid: Boolean, message: String)
     */
    private fun validateShaFormat(sha: String): Pair<Boolean, String> {
        return when {
            sha.isBlank() -> Pair(false, "SHA is required and cannot be empty")
            sha.length != 64 -> Pair(false, "SHA must be exactly 64 characters long")
            !sha.matches(Regex("[a-f0-9]{64}")) -> Pair(false, "SHA must contain only hexadecimal characters (0-9, a-f)")
            else -> Pair(true, "")
        }
    }

    /**
     * Generate a SHA256 hash from an input string.
     *
     * @param input the input string to hash
     * @return the SHA256 hash as a hexadecimal string
     */
    private fun generateSha(input: String): String {
        val messageDigest = MessageDigest.getInstance("SHA-256")
        val hashBytes = messageDigest.digest(input.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
}
