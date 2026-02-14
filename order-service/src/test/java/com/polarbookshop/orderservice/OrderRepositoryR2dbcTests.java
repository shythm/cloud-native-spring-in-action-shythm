package com.polarbookshop.orderservice;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.r2dbc.test.autoconfigure.DataR2dbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import com.polarbookshop.orderservice.config.DataConfig;
import com.polarbookshop.orderservice.order.domain.OrderRepository;
import com.polarbookshop.orderservice.order.domain.OrderService;
import com.polarbookshop.orderservice.order.domain.OrderStatus;

import reactor.test.StepVerifier;

@DataR2dbcTest
@Import(DataConfig.class)
@Testcontainers
public class OrderRepositoryR2dbcTests {

	@Container
	static PostgreSQLContainer postgresql =
		new PostgreSQLContainer(DockerImageName.parse("postgres:14.4"));

	@Autowired
	private OrderRepository orderRepository;

	@DynamicPropertySource
	static void postgresqlProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.r2dbc.url", OrderRepositoryR2dbcTests::r2dbcUrl);
		registry.add("spring.r2dbc.username", postgresql::getUsername);
		registry.add("spring.r2dbc.password", postgresql::getPassword);
		registry.add("spring.flyway.url", postgresql::getJdbcUrl);
	}

	private static String r2dbcUrl() {
		return String.format("r2dbc:postgresql://%s:%s/%s",
			postgresql.getHost(),
			postgresql.getMappedPort(PostgreSQLContainer.POSTGRESQL_PORT),
			postgresql.getDatabaseName()
		);
	}

	@Test
	void createRejectedOrder() {
		// Arrange
		var rejectedOrder = OrderService.buildRejectOrder("1234567890", 3);

		// Act
		var order = orderRepository.save(rejectedOrder);

		// Arrange
		StepVerifier.create(order)
			.expectNextMatches(o -> o.status().equals(OrderStatus.REJECTED))
			.verifyComplete();
	}
}
