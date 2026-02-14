package com.polarbookshop.orderservice;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.polarbookshop.orderservice.order.domain.Order;
import com.polarbookshop.orderservice.order.domain.OrderService;
import com.polarbookshop.orderservice.order.domain.OrderStatus;
import com.polarbookshop.orderservice.order.web.OrderController;
import com.polarbookshop.orderservice.order.web.OrderRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import reactor.core.publisher.Mono;

@WebFluxTest(OrderController.class)
public class OrderControllerWebFluxTests {

	@Autowired
	private WebTestClient webClient;

	@MockitoBean
	private OrderService orderService;

	@Test
	void whenBookNotAvailableThenRejectOrder() {
		// Arrange
		var orderRequest = new OrderRequest("1234567890", 3);
		var expectedOrder = OrderService.buildRejectOrder(
			orderRequest.isbn(), orderRequest.quantity()
		);
		given(orderService.submitOrder(
			orderRequest.isbn(), orderRequest.quantity())
		).willReturn(Mono.just(expectedOrder));

		// Act
		var response = webClient
			.post()
			.uri("/orders")
			.bodyValue(orderRequest)
			.exchange();

		// Assert
		response
			.expectStatus().is2xxSuccessful()
			.expectBody(Order.class).value(actualOrder -> {
				assertThat(actualOrder).isNotNull();
				assertThat(actualOrder.status()).isEqualTo(OrderStatus.REJECTED);
			});
	}
}
