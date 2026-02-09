package com.polarbookshop.orderservice.order.web

import com.polarbookshop.orderservice.order.domain.Order
import com.polarbookshop.orderservice.order.domain.OrderService
import jakarta.validation.Valid
import kotlinx.coroutines.flow.Flow
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("orders")
class OrderController(
    val orderService: OrderService
) {
    @GetMapping
    fun getAllOrders(): Flow<Order> {
        return orderService.getAllOrders()
    }

    @PostMapping
    suspend fun submitOrder(
        @RequestBody @Valid orderRequest: OrderRequest
    ): Order {
        return orderService.submitOrder(orderRequest.isbn, orderRequest.quantity)
    }
}