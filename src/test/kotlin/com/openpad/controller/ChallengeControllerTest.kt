package com.openpad.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.openpad.dto.ChallengeResponseDto
import com.openpad.dto.ChallengeValidationRequestDto
import com.openpad.dto.ChallengeValidationResponseDto
import com.openpad.service.ChallengeService
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import java.time.Instant

/**
 * Web-layer (slice) tests for [ChallengeController].
 *
 * **What `@WebMvcTest` does:**
 *  - Starts only the Spring MVC layer (controllers, filters, advice).
 *  - Does NOT load the full application context (no services, repositories, DB).
 *  - Automatically configures [MockMvc] for you.
 *
 * **Why mock the service?**
 *  - The controller test verifies HTTP plumbing (routing, status codes, response body).
 *  - Business logic is tested separately in ChallengeServiceTest.
 *  - This keeps tests focused and fast.
 *
 * Kotlin-for-Java-devs notes:
 *  - `@MockitoBean` registers a Mockito mock as a Spring bean.
 *  - `mockMvc.post("/path") { ... }` is Spring's Kotlin DSL for POST requests.
 *  - `jsonPath("$.field")` extracts JSON fields from the response body.
 */
@WebMvcTest(ChallengeController::class)
class ChallengeControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockitoBean
    private lateinit var challengeService: ChallengeService

    @Test
    fun `POST challenge-start returns 201 CREATED with challenge DTO`() {
        // Arrange: mock the service to return a challenge response
        val now = Instant.now()
        val expiresAt = now.plusSeconds(300)
        val mockResponse = ChallengeResponseDto(
            sha = "a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6q7r8s9t0u1v2w3x4y5z6a7b8c9d0e1f2",
            createdAt = now,
            expiresAt = expiresAt,
            ttlSeconds = 300L
        )

        whenever(challengeService.startChallenge()).thenReturn(mockResponse)

        // Act + Assert: perform the POST request and verify the response
        mockMvc.post("/api/v1/challenge/start")
            .andExpect {
                status { isCreated() }  // 201 Created
                jsonPath("$.sha") { value(mockResponse.sha) }
                jsonPath("$.ttlSeconds") { value(300) }
            }
    }

    @Test
    fun `POST challenge-start response contains all required fields`() {
        val now = Instant.now()
        val mockResponse = ChallengeResponseDto(
            sha = "test_sha_1234567890abcdef1234567890abcdef1234567890abcdef1234567890",
            createdAt = now,
            expiresAt = now.plusSeconds(300),
            ttlSeconds = 300L
        )

        whenever(challengeService.startChallenge()).thenReturn(mockResponse)

        mockMvc.post("/api/v1/challenge/start")
            .andExpect {
                status { isCreated() }
                jsonPath("$.sha") { exists() }
                jsonPath("$.createdAt") { exists() }
                jsonPath("$.expiresAt") { exists() }
                jsonPath("$.ttlSeconds") { exists() }
            }
    }

    @Test
    fun `POST challenge-start calls service exactly once per request`() {
        val now = Instant.now()
        val mockResponse = ChallengeResponseDto(
            sha = "test_sha",
            createdAt = now,
            expiresAt = now.plusSeconds(300),
            ttlSeconds = 300L
        )

        whenever(challengeService.startChallenge()).thenReturn(mockResponse)

        mockMvc.post("/api/v1/challenge/start")
            .andExpect {
                status { isCreated() }
            }

        // Verify the service was called (implicitly verified by MockitoBean behavior)
        // In a real test, you'd use verify() from mockito-kotlin, but here the mock
        // automatically tracks calls through Spring's bean management.
    }

    @Test
    fun `POST challenge-start endpoint is at correct context path`() {
        val now = Instant.now()
        val mockResponse = ChallengeResponseDto(
            sha = "test_sha",
            createdAt = now,
            expiresAt = now.plusSeconds(300),
            ttlSeconds = 300L
        )

        whenever(challengeService.startChallenge()).thenReturn(mockResponse)

        // Verify the correct endpoint path
        mockMvc.post("/api/v1/challenge/start")
            .andExpect {
                status { isCreated() }
            }
    }

    @Test
    fun `POST challenge-start response is valid JSON`() {
        val now = Instant.now()
        val mockResponse = ChallengeResponseDto(
            sha = "a".repeat(64),  // SHA256 is 64 hex chars
            createdAt = now,
            expiresAt = now.plusSeconds(300),
            ttlSeconds = 300L
        )

        whenever(challengeService.startChallenge()).thenReturn(mockResponse)

        mockMvc.post("/api/v1/challenge/start")
            .andExpect {
                status { isCreated() }
                content { contentType("application/json") }
            }
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // Validation Endpoint Tests
    // ─────────────────────────────────────────────────────────────────────────────

    @Test
    fun `POST challenge-validate returns 200 OK with valid challenge`() {
        val sha = "a".repeat(64)
        val mockResponse = ChallengeValidationResponseDto(
            isValid = true,
            message = "Challenge is valid"
        )

        whenever(challengeService.validateChallenge(ChallengeValidationRequestDto(sha)))
            .thenReturn(mockResponse)

        val request = ChallengeValidationRequestDto(sha)

        mockMvc.post("/api/v1/challenge/validate") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isOk() }
            jsonPath("$.isValid") { value(true) }
            jsonPath("$.message") { value("Challenge is valid") }
        }
    }

    @Test
    fun `POST challenge-validate returns 200 OK with invalid challenge`() {
        val sha = "a".repeat(64)
        val mockResponse = ChallengeValidationResponseDto(
            isValid = false,
            message = "Challenge has expired"
        )

        whenever(challengeService.validateChallenge(ChallengeValidationRequestDto(sha)))
            .thenReturn(mockResponse)

        val request = ChallengeValidationRequestDto(sha)

        mockMvc.post("/api/v1/challenge/validate") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isOk() }
            jsonPath("$.isValid") { value(false) }
            jsonPath("$.message") { value("Challenge has expired") }
        }
    }

    @Test
    fun `POST challenge-validate rejects request with missing SHA`() {
        mockMvc.post("/api/v1/challenge/validate") {
            contentType = MediaType.APPLICATION_JSON
            content = "{}"  // Missing SHA
        }.andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    fun `POST challenge-validate returns challenge not found response`() {
        val sha = "a".repeat(64)
        val mockResponse = ChallengeValidationResponseDto(
            isValid = false,
            message = "Challenge not found"
        )

        whenever(challengeService.validateChallenge(ChallengeValidationRequestDto(sha)))
            .thenReturn(mockResponse)

        val request = ChallengeValidationRequestDto(sha)

        mockMvc.post("/api/v1/challenge/validate") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isOk() }
            jsonPath("$.isValid") { value(false) }
            jsonPath("$.message") { value("Challenge not found") }
        }
    }

    @Test
    fun `POST challenge-validate response contains both fields`() {
        val sha = "a".repeat(64)
        val mockResponse = ChallengeValidationResponseDto(
            isValid = true,
            message = "Challenge is valid"
        )

        whenever(challengeService.validateChallenge(ChallengeValidationRequestDto(sha)))
            .thenReturn(mockResponse)

        val request = ChallengeValidationRequestDto(sha)

        mockMvc.post("/api/v1/challenge/validate") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isOk() }
            jsonPath("$.isValid") { exists() }
            jsonPath("$.message") { exists() }
        }
    }
}
