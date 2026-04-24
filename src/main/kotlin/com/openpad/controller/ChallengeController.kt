package com.openpad.controller

import com.openpad.dto.ChallengeResponseDto
import com.openpad.dto.ChallengeValidationRequestDto
import com.openpad.dto.ChallengeValidationResponseDto
import com.openpad.service.ChallengeService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * REST controller for challenge-related endpoints.
 *
 * **Responsibilities:**
 *  - Map HTTP requests to service calls.
 *  - Translate service results into HTTP responses.
 *  - No business logic here — that lives in ChallengeService.
 *
 * **Why constructor injection?**
 *  - Single constructor parameter → Spring auto-injects ChallengeService.
 *  - No @Autowired needed; Kotlin constructor injection is idiomatic.
 *  - The service is immutable (private val) and thread-safe.
 *
 * Kotlin-for-Java-devs notes:
 *  - `@PostMapping` is identical to `@RequestMapping(method = RequestMethod.POST)`.
 *  - Spring's `kotlin.plugin.spring` opens this class for proxying automatically.
 */
@RestController
@RequestMapping("/api/v1/challenge")
class ChallengeController(private val challengeService: ChallengeService) {

    /**
     * Start a new challenge session.
     *
     * **Endpoint:** `POST /api/v1/challenge/start`
     *
     * **Response:**
     * ```json
     * {
     *   "sha": "a1b2c3d4...",
     *   "createdAt": "2026-03-28T10:30:00Z",
     *   "expiresAt": "2026-03-28T10:35:00Z",
     *   "ttlSeconds": 300
     * }
     * ```
     *
     * **Idempotency:** This is a POST endpoint that creates a new challenge each time.
     * Repeated calls produce different SHAs and timestamps (not idempotent by design).
     *
     * @return [ResponseEntity] with status 201 CREATED and the challenge DTO in the body
     */
    @PostMapping("/start")
    fun startChallenge(): ResponseEntity<ChallengeResponseDto> {
        val challengeResponse = challengeService.startChallenge()
        return ResponseEntity.status(HttpStatus.CREATED).body(challengeResponse)
    }

    /**
     * Validate an existing challenge.
     *
     * **Endpoint:** `POST /api/v1/challenge/validate`
     *
     * **Request Body:**
     * ```json
     * {
     *   "sha": "a1b2c3d4...",
     *   "createdAt": "2026-03-28T10:30:00Z"
     * }
     * ```
     *
     * **Response (Valid Challenge - 200 OK):**
     * ```json
     * {
     *   "isValid": true,
     *   "message": "Challenge is valid"
     * }
     * ```
     *
     * **Response (Invalid/Expired - 200 OK):**
     * ```json
     * {
     *   "isValid": false,
     *   "message": "Challenge has expired"
     * }
     * ```
     *
     * **Validation Logic:**
     *  1. Validate both `sha` and `createdAt` are present in request
     *  2. Validate SHA format (64 hex characters)
     *  3. Look up the challenge by SHA in the database
     *  4. Verify the createdAt matches the database record
     *  5. Check if current time exceeds expiresAt (TTL breach)
     *  6. Return validation result
     *
     * **Error Cases:**
     *  - Missing SHA → "SHA is required and cannot be empty"
     *  - Invalid SHA format → "SHA must be exactly 64 characters long"
     *  - SHA not found → "Challenge not found"
     *  - Timestamp mismatch → "Challenge creation timestamp mismatch"
     *  - Challenge expired → "Challenge has expired"
     *
     * @param request [ChallengeValidationRequestDto] containing SHA and createdAt
     * @return [ResponseEntity] with status 200 and validation result
     */
    @PostMapping("/validate")
    fun validateChallenge(
        @RequestBody request: ChallengeValidationRequestDto
    ): ResponseEntity<ChallengeValidationResponseDto> {
        val validationResponse = challengeService.validateChallenge(request)
        return ResponseEntity.ok(validationResponse)
    }
}
