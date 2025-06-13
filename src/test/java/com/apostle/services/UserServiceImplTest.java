package com.apostle.services;

import com.apostle.Main;
import com.apostle.data.models.Book;
import com.apostle.data.repositories.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = {Main.class, BookRepository.class, AdminServiceImpl.class, UserServiceImpl.class, BookSearchService.class})
public class UserServiceImplTest {

    @Autowired
    private UserServiceImpl userService;
    @Autowired
    private AdminServiceImpl adminService;
    @Autowired
    private BookRepository bookRepository;

    @BeforeEach
    public void cleanDb(){
        bookRepository.deleteAll();
    }
    @Test
    public void searchBooks_test(){
        Book bookOne = new Book();
        bookOne.setTitle("Happy worldwide");
        bookOne.setAuthor("Apostle");
        bookOne.setPublisher("Ola");
        bookOne.setIsbn("12345678901");
        bookOne.setYearPublished(2021);

        Book bookTwo = new Book();
        bookTwo.setTitle("Happy world");
        bookTwo.setAuthor("Apostle");
        bookTwo.setPublisher("Ola");
        bookTwo.setIsbn("193456782906");
        bookTwo.setYearPublished(2021);
         adminService.addBook(bookTwo);
         adminService.addBook(bookOne);

        List<Book> foundBooks = userService.searchBooks("Happy world");
        assertNotNull(foundBooks);
        assertEquals(2, foundBooks.size());

    }

    @Test
    public void searchBooksReturnsEmptyListIfNoBookFound_test(){
        List<Book> foundBooks = userService.searchBooks("Happy world");
        assertNotNull(foundBooks);
        assertEquals(0, foundBooks.size());
    }

    @Test
    public void viewAllAvailableBook_test(){
        Book bookOne = new Book();
        bookOne.setTitle("Happy world");
        bookOne.setAuthor("Apostle");
        bookOne.setPublisher("Ola");
        bookOne.setIsbn("12345678901");
        bookOne.setYearPublished(2021);

        Book bookTwo = new Book();
        bookTwo.setTitle("Happy world");
        bookTwo.setAuthor("Apostle J.A");
        bookTwo.setPublisher("Ola ola");
        bookTwo.setIsbn("193456782906");
        bookTwo.setYearPublished(2021);
        adminService.addBook(bookTwo);
        adminService.addBook(bookOne);

        List<Book> allBooks = userService.viewAllAvailableBooks();
        assertNotNull(allBooks);
        System.out.println(allBooks);
        assertEquals(2, allBooks.size());
        assertEquals(bookOne.getTitle(), allBooks.get(0).getTitle());
    }

    @Test
    public void viewAllAvailableBookReturnsEmptyListIfNoBookFound_test(){
        List<Book> allBooks = userService.viewAllAvailableBooks();
        assertNotNull(allBooks);
        assertEquals(0, allBooks.size());
    }

}