package com.openpad.controller

import com.openpad.service.HelloService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * REST controller that exposes the Hello World endpoint.
 *
 * Responsibilities:
 *  - Map HTTP requests to service calls.
 *  - Translate service results into HTTP responses.
 *  - Nothing else. No business logic lives here.
 *
 * Kotlin-for-Java-devs notes:
 *  - Constructor injection is declared directly in the class header:
 *    `class Foo(private val bar: Bar)`. No `@Autowired` needed with a single constructor.
 *  - `private val` makes the field immutable (like `private final` in Java).
 *  - Spring's `kotlin.plugin.spring` Gradle plugin opens this class for proxying
 *    automatically, so no `open` keyword is needed here.
 */
@RestController
@RequestMapping("/api/v1")
class HelloController(private val helloService: HelloService) {

    /**
     * Returns a plain-text greeting.
     *
     * **Idempotency:** This is an HTTP GET endpoint. Repeated identical requests
     * always return the same response and produce zero side-effects, satisfying
     * both the HTTP GET contract and idempotency by definition.
     *
     * Example:
     * ```
     * GET /api/v1/hello
     * 200 OK
     * Hello, World!
     * ```
     *
     * @return [ResponseEntity] with status 200 and the greeting in the body
     */
    @GetMapping("/hello")
    fun hello(): ResponseEntity<String> = ResponseEntity.ok(helloService.greet())
}
