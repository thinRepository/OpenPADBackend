package com.openpad.controller

import com.openpad.service.HelloService
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

/**
 * Web-layer (slice) tests for [HelloController].
 *
 * **What `@WebMvcTest` does:**
 *  - Starts only the Spring MVC layer (controllers, filters, advice).
 *  - Does NOT load the full application context (no services, repositories, DB).
 *  - Automatically configures [MockMvc] for you.
 *
 * **Why mock the service?**
 *  - The controller test should verify HTTP plumbing (routing, status codes,
 *    response body mapping), not business logic.
 *  - Mocking [HelloService] isolates the controller from any future service changes.
 *
 * Kotlin-for-Java-devs notes:
 *  - `@MockitoBean` (Spring Boot 3.4+) replaces the older `@MockBean`; it registers
 *    a Mockito mock as a Spring bean in the test context.
 *  - `whenever(mock.method()).thenReturn(value)` is mockito-kotlin's null-safe alias
 *    for Mockito's `when(...).thenReturn(...)` (`when` is a reserved keyword in Kotlin).
 *  - `mockMvc.get("/path") { ... }` is Spring's Kotlin DSL for MockMvc — more concise
 *    than the Java `mockMvc.perform(get(...)).andExpect(...)` chain.
 */
@WebMvcTest(HelloController::class)
class HelloControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var helloService: HelloService

    @Test
    fun `GET api-v1-hello returns 200 with greeting`() {
        // Arrange: tell the mock what to return
        whenever(helloService.greet()).thenReturn("Hello, World!")

        // Act + Assert: perform the HTTP request and verify the response
        mockMvc.get("/api/v1/hello")
            .andExpect {
                status { isOk() }
                content { string("Hello, World!") }
            }
    }

    @Test
    fun `GET api-v1-hello is idempotent - second call returns same response`() {
        whenever(helloService.greet()).thenReturn("Hello, World!")

        // Two independent requests must produce identical HTTP responses.
        repeat(2) {
            mockMvc.get("/api/v1/hello")
                .andExpect {
                    status { isOk() }
                    content { string("Hello, World!") }
                }
        }
    }
}
