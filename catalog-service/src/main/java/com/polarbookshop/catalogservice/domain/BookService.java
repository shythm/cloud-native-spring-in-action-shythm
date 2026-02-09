package com.polarbookshop.catalogservice.domain;

import org.springframework.stereotype.Service;

@Service
public class BookService {
	private final BookRepository bookRepository;

	public BookService(BookRepository bookRepository) {
		this.bookRepository = bookRepository;
	}

	/**
	 * 카탈로그에서 도서 목록 보기
	 */
	public Iterable<Book> viewBookList() {
		return bookRepository.findAll();
	}

	/**
	 * 국제 표준 도서 번호(ISBN)로 도서 검색
	 */
	public Book viewBookDetails(String isbn) {
		return bookRepository.findByIsbn(isbn)
			.orElseThrow(() -> new BookNotFoundException(isbn));
	}

	/**
	 * 카탈로그에 새 도서 추가
	 */
	public Book addBookToCatalog(Book book) {
		if (bookRepository.existsByIsbn(book.isbn())) {
			throw new BookAlreadyExistsException(book.isbn());
		}
		return bookRepository.save(book);
	}

	/**
	 * 도서에 대한 기존 정보 편집
	 */
	public Book editBookDetails(String isbn, Book book) {
		return bookRepository.findByIsbn(isbn)
			.map(existingBook -> {
				var bookToUpdate = new Book(
					existingBook.id(),
					existingBook.isbn(),
					book.title(),
					book.author(),
					book.price(),
					book.publisher(),
					existingBook.createdDate(),
					existingBook.lastModifiedDate(),
					existingBook.version()
				);
				return bookRepository.save(bookToUpdate);
			})
			.orElseGet(() -> addBookToCatalog(book));
	}

	/**
	 * 카탈로그에서 도서 삭제
	 */
	public void removeBookFromCatalog(String isbn) {
		bookRepository.deleteByIsbn(isbn);
	}
}
