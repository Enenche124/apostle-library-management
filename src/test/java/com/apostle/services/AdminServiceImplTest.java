package com.apostle.services;

import com.apostle.Main;
import com.apostle.data.models.Book;
import com.apostle.data.models.Role;
import com.apostle.data.repositories.AdminRepository;
import com.apostle.data.repositories.BookRepository;
import com.apostle.dtos.requests.RegisterRequest;
import com.apostle.exceptions.BookNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = {Main.class, AdminServiceImpl.class, BookRepository.class, AdminRepository.class, AuthenticationServiceImpl.class})
public class AdminServiceImplTest {

    @Autowired
    private AdminServiceImpl adminService;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private AuthenticationServiceImpl authenticationService;

    @BeforeEach
    public void setUp() {
        bookRepository.deleteAll();
        adminRepository.deleteAll();
        SecurityContextHolder.clearContext();
    }

    @Test
    public void addBook_test() {

        RegisterRequest adminRequest = new RegisterRequest();
        adminRequest.setUsername("Admin1");
        adminRequest.setEmail("admin1@gmail.com");
        adminRequest.setPassword("Admin1@2023");
        adminRequest.setRole(Role.ADMIN);
        authenticationService.register(adminRequest);

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "admin1@gmail.com", null, Collections.singleton(new SimpleGrantedAuthority("ROLE_ADMIN")));
        SecurityContextHolder.getContext().setAuthentication(auth);


        Book book = new Book();
        book.setTitle("The mystery World");
        book.setAuthor("Enenche John");
        book.setPublisher("J.A Idoko");
        book.setIsbn("43j5674893065");
        book.setYearPublished(2020);
        Book savedBook = adminService.addBook(book);

        assertNotNull(savedBook);
        assertEquals("The mystery World", savedBook.getTitle());
        assertTrue(bookRepository.existsByIsbn("43j5674893065"));
    }

    @Test
    public void deleteBook_test() {
        RegisterRequest adminRequest = new RegisterRequest();
        adminRequest.setUsername("Admin2");
        adminRequest.setEmail("admin2@gmail.com");
        adminRequest.setPassword("Admin2@2023");
        adminRequest.setRole(Role.ADMIN);
        authenticationService.register(adminRequest);


        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "admin2@gmail.com", null, Collections.singleton(new SimpleGrantedAuthority("ROLE_ADMIN")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        Book book1 = new Book();
        book1.setTitle("Happy World");
        book1.setAuthor("Apostle");
        book1.setPublisher("Ola");
        book1.setIsbn("12345678901");
        book1.setYearPublished(2021);
        adminService.addBook(book1);

        Book book2 = new Book();
        book2.setTitle("Happy World to Live");
        book2.setAuthor("Apostle");
        book2.setPublisher("Ola");
        book2.setIsbn("123456782901");
        book2.setYearPublished(2021);
        Book savedBook2 = adminService.addBook(book2);
        assertNotNull(savedBook2);


        adminService.deleteBook("123456782901");


        assertFalse(bookRepository.existsByIsbn("123456782901"));
        assertTrue(bookRepository.existsByIsbn("12345678901")); // Book1 remains
        assertEquals(1, bookRepository.count());
    }

    @Test
    public void deleteBookThrowsExceptionForNonexistentBook_test() {

        RegisterRequest adminRequest = new RegisterRequest();
        adminRequest.setUsername("Admin3");
        adminRequest.setEmail("admin3@gmail.com");
        adminRequest.setPassword("Admin3@2023");
        adminRequest.setRole(Role.ADMIN);
        authenticationService.register(adminRequest);


        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "admin3@gmail.com", null, Collections.singleton(new SimpleGrantedAuthority("ROLE_ADMIN")));
        SecurityContextHolder.getContext().setAuthentication(auth);


        Book book = new Book();
        book.setTitle("The Gods Are Not to Blame");
        book.setAuthor("Chinua Achebe");
        book.setPublisher("Charity Work");
        book.setIsbn("34425198742");
        book.setYearPublished(1992);
        Book savedBook = adminService.addBook(book);
        assertNotNull(savedBook);

        assertThrows(BookNotFoundException.class, () -> adminService.deleteBook("6739261738"));
    }

    @Test
    public void updateBook_test() {

        RegisterRequest adminRequest = new RegisterRequest();
        adminRequest.setUsername("Admin4");
        adminRequest.setEmail("admin4@gmail.com");
        adminRequest.setPassword("Admin4@2023");
        adminRequest.setRole(Role.ADMIN);
        authenticationService.register(adminRequest);


        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "admin4@gmail.com", null, Collections.singleton(new SimpleGrantedAuthority("ROLE_ADMIN")));
        SecurityContextHolder.getContext().setAuthentication(auth);


        Book book = new Book();
        book.setTitle("My Love Story");
        book.setAuthor("Church Boy");
        book.setPublisher("Church Sisters");
        book.setIsbn("8976534219");
        book.setYearPublished(1992);
        Book savedBook = adminService.addBook(book);
        assertEquals("My Love Story", savedBook.getTitle());


        Book updateBook = new Book();
        updateBook.setTitle("The Love of Money");
        updateBook.setAuthor("Igbo Man");
        updateBook.setPublisher("Biafra");
        updateBook.setIsbn("8976534219");
        updateBook.setYearPublished(2012);
        Book updatedBook = adminService.updateBook("8976534219", updateBook);

        assertNotNull(updatedBook);
        assertEquals("The Love of Money", updatedBook.getTitle());
        assertEquals("Igbo Man", updatedBook.getAuthor());
        assertTrue(bookRepository.existsByIsbn("8976534219"));
    }

    @Test
    public void updateBookThrowsExceptionForNonexistentBook_test() {

        RegisterRequest adminRequest = new RegisterRequest();
        adminRequest.setUsername("Admin5");
        adminRequest.setEmail("admin5@gmail.com");
        adminRequest.setPassword("Admin5@2023");
        adminRequest.setRole(Role.ADMIN);
        authenticationService.register(adminRequest);

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "admin5@gmail.com", null, Collections.singleton(new SimpleGrantedAuthority("ROLE_ADMIN")));
        SecurityContextHolder.getContext().setAuthentication(auth);


        Book book = new Book();
        book.setTitle("The Rich Man in Babylon");
        book.setAuthor("John Smith");
        book.setPublisher("Smith Sons");
        book.setIsbn("8976534219");
        book.setYearPublished(2020);
        Book savedBook = adminService.addBook(book);
        assertNotNull(savedBook);

        Book updateBook = new Book();
        updateBook.setTitle("The Love of Money");
        updateBook.setAuthor("Igbo Man");
        updateBook.setPublisher("Biafra");
        updateBook.setIsbn("000372634352");
        assertThrows(BookNotFoundException.class, () ->
                adminService.updateBook("wrong_isbn", updateBook));
    }

    @Test
    public void searchBook_test() {

        RegisterRequest adminRequest = new RegisterRequest();
        adminRequest.setUsername("Admin6");
        adminRequest.setEmail("admin6@gmail.com");
        adminRequest.setPassword("Admin6@2023");
        adminRequest.setRole(Role.ADMIN);
        authenticationService.register(adminRequest);


        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "admin6@gmail.com", null, Collections.singleton(new SimpleGrantedAuthority("ROLE_ADMIN")));
        SecurityContextHolder.getContext().setAuthentication(auth);


        Book book1 = new Book();
        book1.setTitle("The Rich Man in Babylon");
        book1.setAuthor("John Smith");
        book1.setPublisher("Smith Sons");
        book1.setIsbn("8976534219");
        book1.setYearPublished(2020);
        Book savedBook1 = adminService.addBook(book1);
        assertNotNull(savedBook1);

        Book book2 = new Book();
        book2.setTitle("The Love of Money");
        book2.setAuthor("John Smith");
        book2.setPublisher("Biafra");
        book2.setIsbn("000372634352");
        book2.setYearPublished(2012);
        Book savedBook2 = adminService.addBook(book2);
        assertNotNull(savedBook2);


        List<Book> foundBooks = adminService.searchBooks("John Smith");
        assertNotNull(foundBooks);
        assertEquals(2, foundBooks.size());
        assertTrue(foundBooks.stream().anyMatch(b -> b.getTitle().equals("The Rich Man in Babylon")));
        assertTrue(foundBooks.stream().anyMatch(b -> b.getTitle().equals("The Love of Money")));
    }

    @Test
    public void searchReturnsEmptyListIfNoBookFound_test() {
        // Register admin
        RegisterRequest adminRequest = new RegisterRequest();
        adminRequest.setUsername("Admin7");
        adminRequest.setEmail("admin7@gmail.com");
        adminRequest.setPassword("Admin7@2023");
        adminRequest.setRole(Role.ADMIN);
        authenticationService.register(adminRequest);

        List<Book> foundBooks = adminService.searchBooks("Nonexistent Author");
        assertNotNull(foundBooks);
        assertEquals(0, foundBooks.size());
    }

    @Test
    public void findBookByIsbn_test() {

        RegisterRequest adminRequest = new RegisterRequest();
        adminRequest.setUsername("Admin8");
        adminRequest.setEmail("admin8@gmail.com");
        adminRequest.setPassword("Admin8@2023");
        adminRequest.setRole(Role.ADMIN);
        authenticationService.register(adminRequest);


        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "admin8@gmail.com", null, Collections.singleton(new SimpleGrantedAuthority("ROLE_ADMIN")));
        SecurityContextHolder.getContext().setAuthentication(auth);


        Book book = new Book();
        book.setTitle("My Good Friend");
        book.setAuthor("Uncle Bob");
        book.setPublisher("Uncle Bob's Sons");
        book.setIsbn("983764512273");
        book.setYearPublished(2020);
        Book savedBook = adminService.addBook(book);
        assertNotNull(savedBook);


        Book foundBook = adminService.findBookByIsbn("983764512273");
        assertNotNull(foundBook);
        assertEquals("My Good Friend", foundBook.getTitle());
    }

    @Test
    public void findBookByIsbnThrowsExceptionIfNoBookFound_test() {
        // Register admin
        RegisterRequest adminRequest = new RegisterRequest();
        adminRequest.setUsername("Admin9");
        adminRequest.setEmail("admin9@gmail.com");
        adminRequest.setPassword("Admin9@2023");
        adminRequest.setRole(Role.ADMIN);
        authenticationService.register(adminRequest);


        assertThrows(BookNotFoundException.class, () ->
                adminService.findBookByIsbn("1234567890"));
    }

    @Test
    public void viewAllBooks_test() {

        RegisterRequest adminRequest = new RegisterRequest();
        adminRequest.setUsername("Admin10");
        adminRequest.setEmail("admin10@gmail.com");
        adminRequest.setPassword("Admin10@2023");
        adminRequest.setRole(Role.ADMIN);
        authenticationService.register(adminRequest);


        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "admin10@gmail.com", null, Collections.singleton(new SimpleGrantedAuthority("ROLE_ADMIN")));
        SecurityContextHolder.getContext().setAuthentication(auth);


        Book book1 = new Book();
        book1.setTitle("My Good Friend");
        book1.setAuthor("Uncle Bob");
        book1.setPublisher("Uncle Bob's Sons");
        book1.setIsbn("983764512273");
        book1.setYearPublished(2020);
        Book savedBook1 = adminService.addBook(book1);
        assertNotNull(savedBook1);

        Book book2 = new Book();
        book2.setTitle("When Men Slept");
        book2.setAuthor("Apostle");
        book2.setPublisher("J.A Idoko");
        book2.setIsbn("000372634352");
        book2.setYearPublished(2026);
        Book savedBook2 = adminService.addBook(book2);
        assertNotNull(savedBook2);


        List<Book> allBooks = adminService.viewAllBooks();
        assertNotNull(allBooks);
        assertEquals(2, allBooks.size());
        assertTrue(allBooks.stream().anyMatch(b -> b.getTitle().equals("My Good Friend")));
        assertTrue(allBooks.stream().anyMatch(b -> b.getTitle().equals("When Men Slept")));
    }

    @Test
    public void viewAllBooksReturnsEmptyListIfNoBookFound_test() {

        RegisterRequest adminRequest = new RegisterRequest();
        adminRequest.setUsername("Admin11");
        adminRequest.setEmail("admin11@gmail.com");
        adminRequest.setPassword("Admin11@2023");
        adminRequest.setRole(Role.ADMIN);
        authenticationService.register(adminRequest);


        List<Book> allBooks = adminService.viewAllBooks();
        assertNotNull(allBooks);
        assertEquals(0, allBooks.size());
    }
}