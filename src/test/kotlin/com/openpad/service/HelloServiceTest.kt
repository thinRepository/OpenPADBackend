package com.openpad.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * Unit tests for [HelloService].
 *
 * No Spring context is loaded — this is a plain JUnit 5 test.
 * Fast, isolated, and focused solely on the service's behaviour.
 *
 * Kotlin-for-Java-devs notes:
 *  - JUnit 5 annotations (`@Test`, `@BeforeEach`, etc.) are identical to Java.
 *  - `fun` replaces `void`; return type is inferred as `Unit` (≈ Java `void`).
 *  - Backtick-quoted function names allow natural-language test descriptions:
 *    `fun `greet returns Hello World`()` — very readable in test reports.
 */
class HelloServiceTest {

    // System Under Test — instantiated directly, no mocking needed here
    private val service = HelloService()

    @Test
    fun `greet returns Hello World`() {
        val result = service.greet()
        assertEquals("Hello, World!", result)
    }

    @Test
    fun `greet is idempotent - repeated calls return same value`() {
        // Calling greet() multiple times must always produce the same output.
        val first = service.greet()
        val second = service.greet()
        assertEquals(first, second)
    }
}
