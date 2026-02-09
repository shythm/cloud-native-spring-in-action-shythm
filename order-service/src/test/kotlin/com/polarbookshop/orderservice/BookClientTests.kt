package com.polarbookshop.orderservice

import com.polarbookshop.orderservice.book.BookClient
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient
import kotlin.test.assertEquals

class BookClientTests {
    private lateinit var mockWebServer: MockWebServer
    private lateinit var bookClient: BookClient

    @BeforeEach
    fun setup() {
        this.mockWebServer = MockWebServer()
        this.mockWebServer.start()

        val webClient = WebClient.builder()
            .baseUrl(mockWebServer.url("/").toUri().toString())
            .build()

        this.bookClient = BookClient(webClient)
    }

    @AfterEach
    fun clean() {
        this.mockWebServer.shutdown()
    }

    @Test
    fun henBookExistsThenReturnBook() = runTest {
        // Arrange
        val bookIsbn = "1234567890"
        val mockResponse = MockResponse()
            .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .setBody(
                """
                {
                    "isbn": $bookIsbn,
                    "title": "Title",
                    "author": "Author",
                    "price": 9.90,
                    "publisher": "Polarsophia"
                }
                """.trimIndent()
            )
        mockWebServer.enqueue(mockResponse)

        // Act
        val book = bookClient.getBookByIsbn(bookIsbn)

        // Assert
        assertEquals(book?.isbn, bookIsbn)
    }
}