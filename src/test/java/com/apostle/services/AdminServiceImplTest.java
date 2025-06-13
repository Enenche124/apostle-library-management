package com.apostle.services;

import com.apostle.Main;
import com.apostle.data.models.Book;
import com.apostle.data.repositories.BookRepository;
import com.apostle.exceptions.BookNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = {Main.class, AdminServiceImpl.class, BookRepository.class})
public class AdminServiceImplTest {

    @Autowired
    private AdminServiceImpl adminService;

    @Autowired
    private BookRepository bookRepository;

    @BeforeEach
    public void setUp() {
        bookRepository.deleteAll();
    }
    @Test
    public void addBook_test(){
        Book book = new Book();
        book.setTitle("The dramatic world");
        book.setAuthor("Enenche John");
        book.setPublisher("J.A idoko");
        book.setIsbn("43j5674893065");
        book.setYearPublished(2020);
        Book savedBook = adminService.addBook(book);
        assertNotNull(savedBook);
        assertEquals(book.getTitle(),savedBook.getTitle());
    }


    @Test
    public void deleteBook_test(){
        Book book = new Book();
        book.setTitle("Happy  world");
        book.setAuthor("Apostle");
        book.setPublisher("Ola");
        book.setIsbn("12345678901");
        book.setYearPublished(2021);

        Book bookTwo = new Book();
        bookTwo.setTitle("Happy  world to live");
        bookTwo.setAuthor("Apostle");
        bookTwo.setPublisher("Ola");
        bookTwo.setIsbn("123456782901");
        bookTwo.setYearPublished(2021);
        Book savedBook = adminService.addBook(bookTwo);
        adminService.addBook(book);
        System.out.println(savedBook);
        assertNotNull(savedBook);
        adminService.deleteBook(savedBook.getIsbn());
        assertFalse(bookRepository.existsByIsbn(savedBook.getIsbn()));
        assertEquals(1,  adminService.count());
    }
    @Test
    public void deleteBookThrowsExceptionForNonexistentBook_test(){
        Book book = new Book();
        book.setTitle("The god's are not to be blame");
        book.setAuthor("Chinua Achebe");
        book.setPublisher("Charity work");
        book.setIsbn("34425198742");
        book.setYearPublished(1992);
        Book savedBook = adminService.addBook(book);
        assertNotNull(savedBook);
        assertThrows(BookNotFoundException.class, () -> adminService.deleteBook("6739261738"));
    }

    @Test
    public void updateBook_test(){
        Book book = new Book();
        book.setTitle("My love story");
        book.setAuthor("Church boy");
        book.setPublisher("Church sisters");
        book.setIsbn("8976534219");
        book.setYearPublished(1992);

        Book savedBook = adminService.addBook(book);
        assertEquals("My love story", savedBook.getTitle());

        Book updateBook = new Book();
        updateBook.setTitle("The love of money");
        updateBook.setAuthor("Igbo man");
        updateBook.setPublisher("Biafra");
        updateBook.setIsbn("000372634352");
        updateBook.setYearPublished(2012);

       Book updatedBook = adminService.updateBook("8976534219", updateBook);
        System.out.println(updatedBook);

       assertEquals("The love of money", updatedBook.getTitle());

    }

    @Test
    public void updateBookThrowsExceptionForNonexistentBook_test(){
        Book book = new Book();
        book.setTitle("The rich man in babylon");
        book.setAuthor("John smith");
        book.setPublisher("Smith son's");
        book.setIsbn("8976534219");
        book.setYearPublished(2020);
        Book savedBook = adminService.addBook(book);
        assertNotNull(savedBook);

        Book updateBook = new Book();
        updateBook.setTitle("The love of money");
        updateBook.setAuthor("Igbo man");
        updateBook.setPublisher("Biafra");
        updateBook.setIsbn("000372634352");
        updateBook.setYearPublished(2012);

        assertThrows(BookNotFoundException.class,
                () -> adminService.updateBook("Wrong_isbn", updateBook));
    }

    @Test
    public void searchBook_test(){
        Book bookOne = new Book();
        bookOne.setTitle("The rich man in babylon");
        bookOne.setAuthor("John smith");
        bookOne.setPublisher("Smith son's");
        bookOne.setIsbn("8976534219");
        bookOne.setYearPublished(2020);
        Book savedBookOne = adminService.addBook(bookOne);
        assertNotNull(savedBookOne);

        Book bookTwo = new Book();
        bookTwo.setTitle("The love of money");
        bookTwo.setAuthor("John smith");
        bookTwo.setPublisher("Biafra");
        bookTwo.setIsbn("000372634352");
        bookTwo.setYearPublished(2012);
        Book savedBookTwo = adminService.addBook(bookTwo);
        assertNotNull(savedBookTwo);

        List<Book> foundBooks = adminService.searchBooks("John smith");
        assertNotNull(foundBooks);
        assertEquals(2, foundBooks.size());
        assertEquals(savedBookOne.getTitle(), foundBooks.get(0).getTitle());
        assertEquals(savedBookTwo.getTitle(), foundBooks.get(1).getTitle());

    }

    @Test
    public void searchReturnsEmptyListIfNoBookFound_test(){
        List<Book> foundBooks = adminService.searchBooks("John smith");
        assertNotNull(foundBooks);
        assertEquals(0, foundBooks.size());
    }
    @Test
    public void findBookByIsbn_test(){
        Book bookOne = new Book();
        bookOne.setTitle("My good friend");
        bookOne.setAuthor("Uncle bob");
        bookOne.setPublisher("Uncle bob's son's");
        bookOne.setIsbn("983764512273");
        bookOne.setYearPublished(2020);
        Book savedBookOne = adminService.addBook(bookOne);
        assertNotNull(savedBookOne);

        Book bookTwo = new Book();
        bookTwo.setTitle("When men slept");
        bookTwo.setAuthor("Apostle");
        bookTwo.setPublisher("J.A idoko");
        bookTwo.setIsbn("000372634352");
        bookTwo.setYearPublished(2026);
        Book savedBookTwo = adminService.addBook(bookTwo);
        assertNotNull(savedBookTwo);

        Book foundBook = adminService.findBookByIsbn(savedBookOne.getIsbn());
        assertNotNull(foundBook);
        assertEquals(savedBookOne.getTitle(), foundBook.getTitle());
    }

    @Test
    public void findBookByIsbnThrowsExceptionIfNoBookFound_test(){
        assertThrows(BookNotFoundException.class, () -> adminService.findBookByIsbn("1234567890"));
    }

    @Test
    public void viewAllBooks_test(){
        Book bookOne = new Book();
        bookOne.setTitle("My good friend");
        bookOne.setAuthor("Uncle bob");
        bookOne.setPublisher("Uncle bob's son's");
        bookOne.setIsbn("983764512273");
        bookOne.setYearPublished(2020);
        Book savedBookOne = adminService.addBook(bookOne);
        assertNotNull(savedBookOne);

        Book bookTwo = new Book();
        bookTwo.setTitle("When men slept");
        bookTwo.setAuthor("Apostle");
        bookTwo.setPublisher("J.A idoko");
        bookTwo.setIsbn("000372634352");
        bookTwo.setYearPublished(2026);
        Book savedBookTwo = adminService.addBook(bookTwo);
        assertNotNull(savedBookTwo);
        List<Book> allBooks = adminService.viewAllBooks();
        assertNotNull(allBooks);
        assertEquals(2, allBooks.size());
        assertEquals(savedBookOne.getTitle(), allBooks.get(0).getTitle());
        assertEquals(savedBookTwo.getTitle(), allBooks.get(1).getTitle());
    }

    @Test
    public void viewAllBooksReturnsEmptyListIfNoBookFound_test(){
        List<Book> allBooks = adminService.viewAllBooks();
        assertNotNull(allBooks);
        assertEquals(0, allBooks.size());
    }

}