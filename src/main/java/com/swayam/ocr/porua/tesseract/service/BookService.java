package com.swayam.ocr.porua.tesseract.service;

import java.util.List;

import com.swayam.ocr.porua.tesseract.model.Book;

public interface BookService {

    Book addBook(Book book);

    Book getBook(long bookId);

    List<Book> getBooks();

}
