package com.openpad.service

import com.openpad.dto.ChallengeValidationRequestDto
import com.openpad.entity.Challenge
import com.openpad.repository.ChallengeRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.Instant

/**
 * Unit tests for [ChallengeService].
 *
 * **Testing strategy:**
 *  - Uses mocks for the repository (don't test the database, test the service logic).
 *  - Verifies SHA generation, timestamp handling, and persistence.
 *  - No Spring context loaded — pure JUnit 5 tests (very fast).
 *
 * Kotlin-for-Java-devs notes:
 *  - `mock<Type>()` from mockito-kotlin creates a Mockito mock (shorter than `mock(Type::class.java)`).
 *  - Backtick function names make test descriptions readable: `fun `description`() { }`.
 *  - `argumentCaptor` lets you inspect arguments passed to a mocked method.
 */
class ChallengeServiceTest {

    private val mockRepository = mock<ChallengeRepository> {
        onGeneric { save(any<Challenge>()) }.thenAnswer { invocation ->
            invocation.getArgument<Challenge>(0)
        }
    }
    private val service = ChallengeService(mockRepository)

    @Test
    fun `startChallenge generates non-empty SHA`() {
        // Act
        val response = service.startChallenge()

        // Assert
        assertNotNull(response.sha)
        assertEquals(64, response.sha.length)  // SHA256 in hex is 64 characters
        assertTrue(response.sha.matches(Regex("[a-f0-9]{64}")))  // Only hex characters
    }

    @Test
    fun `startChallenge sets createdAt to current time`() {
        val before = Instant.now()
        val response = service.startChallenge()
        val after = Instant.now()

        // Assert that createdAt is between before and after
        assertTrue(response.createdAt.isAfter(before.minusSeconds(1)))
        assertTrue(response.createdAt.isBefore(after.plusSeconds(1)))
    }

    @Test
    fun `startChallenge sets expiresAt to createdAt plus TTL`() {
        val response = service.startChallenge()

        // Assert that expiresAt is approximately createdAt + TTL
        val expectedExpiry = response.createdAt.plusSeconds(response.ttlSeconds)
        assertTrue(
            response.expiresAt.isBefore(expectedExpiry.plusSeconds(1)) &&
                    response.expiresAt.isAfter(expectedExpiry.minusSeconds(1))
        )
    }

    @Test
    fun `startChallenge persists challenge to repository`() {
        val captor = argumentCaptor<Challenge>()
        whenever(mockRepository.save(captor.capture())).thenAnswer { invocation ->
            invocation.getArgument<Challenge>(0)
        }

        service.startChallenge()

        // Verify repository.save() was called exactly once
        verify(mockRepository).save(any())

        // Verify the challenge was saved with the expected fields
        val savedChallenge = captor.firstValue
        assertNotNull(savedChallenge.sha)
        assertNotNull(savedChallenge.createdAt)
        assertNotNull(savedChallenge.expiresAt)
    }

    @Test
    fun `startChallenge returns DTO with ttlSeconds`() {
        val response = service.startChallenge()

        assertEquals(300L, response.ttlSeconds)  // Default TTL is 300 seconds (5 minutes)
    }

    @Test
    fun `startChallenge generates unique SHAs on repeated calls`() {
        val response1 = service.startChallenge()
        val response2 = service.startChallenge()

        // SHAs should be different (with extremely high probability)
        assertNotNull(response1.sha)
        assertNotNull(response2.sha)
        assertTrue(response1.sha != response2.sha, "SHAs should be unique")
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // Validation Tests
    // ─────────────────────────────────────────────────────────────────────────────

    @Test
    fun `validateChallenge rejects empty SHA`() {
        val request = ChallengeValidationRequestDto(sha = "")

        val response = service.validateChallenge(request)

        assertFalse(response.isValid)
        assertEquals("SHA is required and cannot be empty", response.message)
    }

    @Test
    fun `validateChallenge rejects SHA with incorrect length`() {
        val request = ChallengeValidationRequestDto(sha = "abc123")

        val response = service.validateChallenge(request)

        assertFalse(response.isValid)
        assertEquals("SHA must be exactly 64 characters long", response.message)
    }

    @Test
    fun `validateChallenge rejects SHA with non-hex characters`() {
        val request = ChallengeValidationRequestDto(sha = "z".repeat(64))

        val response = service.validateChallenge(request)

        assertFalse(response.isValid)
        assertEquals("SHA must contain only hexadecimal characters (0-9, a-f)", response.message)
    }

    @Test
    fun `validateChallenge returns not found when SHA doesn't exist in database`() {
        whenever(mockRepository.findBySha(any())).thenReturn(null)

        val request = ChallengeValidationRequestDto(sha = "a".repeat(64))

        val response = service.validateChallenge(request)

        assertFalse(response.isValid)
        assertEquals("Challenge not found", response.message)
    }

    @Test
    fun `validateChallenge rejects when challenge has expired`() {
        val now = Instant.now()
        val challenge = Challenge(
            id = 1,
            sha = "a".repeat(64),
            createdAt = now.minusSeconds(400),
            expiresAt = now.minusSeconds(100)
        )

        whenever(mockRepository.findBySha("a".repeat(64))).thenReturn(challenge)

        val request = ChallengeValidationRequestDto(sha = "a".repeat(64))

        val response = service.validateChallenge(request)

        assertFalse(response.isValid)
        assertEquals("Challenge has expired", response.message)
    }

    @Test
    fun `validateChallenge accepts valid, non-expired challenge`() {
        val now = Instant.now()
        val challenge = Challenge(
            id = 1,
            sha = "a".repeat(64),
            createdAt = now.minusSeconds(100),
            expiresAt = now.plusSeconds(200)
        )

        whenever(mockRepository.findBySha("a".repeat(64))).thenReturn(challenge)

        val request = ChallengeValidationRequestDto(sha = "a".repeat(64))

        val response = service.validateChallenge(request)

        assertTrue(response.isValid)
        assertEquals("Challenge is valid", response.message)
    }

    @Test
    fun `validateChallenge accepts challenge at exact expiration boundary`() {
        val now = Instant.now()
        val expiresAt = now.plusSeconds(1)
        val challenge = Challenge(
            id = 1,
            sha = "b".repeat(64),
            createdAt = now.minusSeconds(299),
            expiresAt = expiresAt
        )

        whenever(mockRepository.findBySha("b".repeat(64))).thenReturn(challenge)

        val request = ChallengeValidationRequestDto(sha = "b".repeat(64))

        val response = service.validateChallenge(request)

        assertTrue(response.isValid)
    }
}
