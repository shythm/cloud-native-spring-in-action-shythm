package com.polarbookshop.orderservice

import com.polarbookshop.orderservice.order.domain.Order
import com.polarbookshop.orderservice.order.domain.OrderService
import com.polarbookshop.orderservice.order.domain.OrderStatus
import com.polarbookshop.orderservice.order.web.OrderController
import com.polarbookshop.orderservice.order.web.OrderRequest
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.reactive.server.WebTestClient
import kotlin.test.assertEquals

@WebFluxTest(OrderController::class)
class OrderControllerWebFluxTests {

    @Autowired
    private lateinit var webClient: WebTestClient

    @MockitoBean
    private lateinit var orderService: OrderService

    @Test
    fun whenBookNotAvailableThenRejectOrder() {
        // Arrange
        val isbn = "1234567890"
        val quantity = 3
        val orderRequest = OrderRequest(isbn, quantity)
        val expectedOrder = OrderService.buildRejectedOrder(isbn, quantity)
        `when`(runBlocking {
            orderService.submitOrder(isbn, quantity)
        }).thenReturn(expectedOrder)

        // Act & Assert
        webClient
            .post()
            .uri("/orders")
            .bodyValue(orderRequest)
            .exchange()
            .expectStatus().is2xxSuccessful
            .expectBody(Order::class.java)
            .value { order ->
                assertNotNull(order)
                assertEquals(OrderStatus.REJECTED, order.status)
            }
    }
}