package io.github.mpecan.upsert.integration.repositories

import io.github.mpecan.upsert.repository.UpsertRepository
import io.github.mpecan.upsert.type.json.test.TestJsonEntity
import org.springframework.data.jpa.repository.JpaRepository

interface TestJsonEntityRepository : UpsertRepository<TestJsonEntity, Long>,
    JpaRepository<TestJsonEntity, Long>