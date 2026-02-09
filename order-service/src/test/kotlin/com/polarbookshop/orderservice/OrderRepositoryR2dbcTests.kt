package com.polarbookshop.orderservice

import com.polarbookshop.orderservice.config.DataConfig
import com.polarbookshop.orderservice.order.domain.OrderRepository
import com.polarbookshop.orderservice.order.domain.OrderService
import com.polarbookshop.orderservice.order.domain.OrderStatus
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import java.time.Duration
import kotlin.test.assertEquals

@DataR2dbcTest
@Import(DataConfig::class)
@Testcontainers
class OrderRepositoryR2dbcTests {

    /*
     * The test failure was caused by two issues:
     * 1. `@JvmStatic` annotation missing → Caused bean initialization order problems where Flyway tried to connect before dynamic properties were registered
     * 2. Database container startup timing → Container was running but PostgreSQL wasn't ready to accept connections
     * Adding `@JvmStatic` to companion object members and implementing proper wait strategies resolved both issues.
     */

    companion object {
        @Container
        @JvmStatic
        private val postgresql = PostgreSQLContainer(DockerImageName.parse("postgres:14.4"))
            .waitingFor(Wait.forListeningPort())
            .withStartupTimeout(Duration.ofSeconds(60))

        @JvmStatic
        @DynamicPropertySource
        fun postgresqlProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.r2dbc.url") { getR2dbcUrl() }
            registry.add("spring.r2dbc.username", postgresql::getUsername)
            registry.add("spring.r2dbc.password", postgresql::getPassword)
            registry.add("spring.flyway.url", postgresql::getJdbcUrl)
        }

        @JvmStatic
        private fun getR2dbcUrl() = String.format(
            "r2dbc:postgresql://%s:%s/%s",
            postgresql.host,
            postgresql.getMappedPort(PostgreSQLContainer.POSTGRESQL_PORT),
            postgresql.databaseName,
        )
    }

    @Autowired
    private lateinit var orderRepository: OrderRepository

    @Test
    fun createRejectedOrder() = runTest {
        // Arrange
        val rejectedOrder = OrderService.buildRejectedOrder("1234567890", 3)

        // Act
        val createdOrder = orderRepository.save(rejectedOrder)

        // Assert
        assertEquals(createdOrder.status, OrderStatus.REJECTED)
    }

}