package com.openpad.repository

import com.openpad.entity.Challenge
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/**
 * Repository for [Challenge] entity.
 *
 * **Why Spring Data JPA?**
 *  - Zero implementation needed — Spring generates SQL at runtime.
 *  - Database-agnostic: Hibernate handles H2, PostgreSQL, MSSQL transparently.
 *  - Adding a new query method (e.g., `findBySha(sha: String)`) requires only a method signature;
 *    Spring derives the implementation from the method name.
 *
 * Kotlin-for-Java-devs notes:
 *  - `Repository<T, ID>` where T is the entity and ID is the primary key type.
 *  - Spring auto-registers any interface extending JpaRepository as a Spring bean.
 *  - No implementation class needed — Spring uses dynamic proxies.
 */
@Repository
interface ChallengeRepository : JpaRepository<Challenge, Long> {
    /**
     * Find a challenge by its SHA.
     *
     * @param sha the challenge's unique SHA256 hash
     * @return the Challenge if found, null otherwise
     */
    fun findBySha(sha: String): Challenge?
}
