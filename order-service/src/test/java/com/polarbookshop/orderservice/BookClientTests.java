package com.polarbookshop.orderservice;

import java.io.IOException;

import org.junit.jupiter.api.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import com.polarbookshop.orderservice.book.BookClient;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import reactor.test.StepVerifier;

public class BookClientTests {
	private MockWebServer mockWebServer;
	private BookClient bookClient;

	@BeforeEach
	void setup() throws IOException {
		this.mockWebServer = new MockWebServer();
		this.mockWebServer.start();
		var webClient = WebClient.builder()
			.baseUrl(mockWebServer.url("/").uri().toString())
			.build();
		this.bookClient = new BookClient(webClient);
	}

	@Test
	void whenBookExistsThenReturnBook() {
		// Arrange
		var bookIsbn = "1234567890";
		var mockResponse = new MockResponse()
			.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
			.setBody("""
			{
				"isbn": %s,
				"title": "Title",
				"author": "Author",
				"price": 9.90,
				"publisher": "Polarsophia"
			}
			""".formatted(bookIsbn));
		mockWebServer.enqueue(mockResponse);

		// Act
		var book = bookClient.getBookByIsbn(bookIsbn);

		// Assert
		StepVerifier.create(book)
			.expectNextMatches(b -> b.isbn().equals(bookIsbn))
			.verifyComplete();
	}

	@AfterEach
	void clean() throws IOException {
		this.mockWebServer.shutdown();
	}
}
