package com.polarbookshop.orderservice.order.domain

import com.polarbookshop.orderservice.book.Book
import com.polarbookshop.orderservice.book.BookClient
import kotlinx.coroutines.flow.Flow
import org.springframework.stereotype.Service

@Service
class OrderService(
    val bookClient: BookClient,
    val orderRepository: OrderRepository,
) {

    companion object {
        fun buildRejectedOrder(bookIsbn: String, quantity: Int): Order {
            return Order(
                bookIsbn = bookIsbn,
                quantity = quantity,
                status = OrderStatus.REJECTED
            )
        }

        fun buildAcceptedOrder(book: Book, quantity: Int): Order {
            return Order(
                bookIsbn = book.isbn,
                bookName = "${book.title} - ${book.author}",
                bookPrice = book.price,
                quantity = quantity,
                status = OrderStatus.ACCEPTED,
            )
        }
    }

    fun getAllOrders(): Flow<Order> {
        return orderRepository.findAll()
    }

    suspend fun submitOrder(isbn: String, quantity: Int): Order {
        val book = bookClient.getBookByIsbn(isbn)
        val order = if (book != null) {
            buildAcceptedOrder(book, quantity)
        } else {
            buildRejectedOrder(isbn, quantity)
        }
        return orderRepository.save(order)
    }
}
