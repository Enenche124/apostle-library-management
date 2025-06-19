package com.apostle.services;

import com.apostle.Main;
import com.apostle.data.models.Book;
import com.apostle.data.models.BorrowBookRecord;
import com.apostle.data.models.BorrowStatus;
import com.apostle.data.models.Role;
import com.apostle.data.repositories.AdminRepository;
import com.apostle.data.repositories.BookRepository;
import com.apostle.data.repositories.BorrowBookRecordRepository;
import com.apostle.data.repositories.UserRepository;
import com.apostle.dtos.requests.LoginRequest;
import com.apostle.dtos.requests.RegisterRequest;
import com.apostle.dtos.responses.BorrowResponse;
import com.apostle.dtos.responses.LoginResponse;
import com.apostle.dtos.responses.RegisterResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = {Main.class, BorrowServiceImpl.class, BookRepository.class, BorrowBookRecordRepository.class, AdminServiceImpl.class, AdminRepository.class, UserRepository.class, AuthenticationServiceImpl.class})
public class BorrowServiceImplTest {

    @Autowired
    private BorrowServiceImpl borrowService;

    @Autowired
    private BorrowBookRecordRepository borrowBookRecordRepository;

    @Autowired
    private AdminServiceImpl adminService;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private AuthenticationServiceImpl authenticationService;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    public void setup(){
        borrowBookRecordRepository.deleteAll();
        bookRepository.deleteAll();
        userRepository.deleteAll();
        adminRepository.deleteAll();
    }

    @Test
    public void borrowBook_test() {
        //User
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("John");
        registerRequest.setEmail("john@gmail.com");
        registerRequest.setPassword("John@2002");
        registerRequest.setRole(Role.USER);
        RegisterResponse registerResponse = authenticationService.register(registerRequest);
        assertTrue(registerResponse.isSuccess());

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("john@gmail.com");
        loginRequest.setPassword("John@2002");
        LoginResponse loginResponse = authenticationService.login(loginRequest);
        assertTrue(loginResponse.isSuccess());

        //ADMIN
        RegisterRequest adminRegisterRequest = new RegisterRequest();
        adminRegisterRequest.setUsername("mike");
        adminRegisterRequest.setEmail("mike@gmail.com");
        adminRegisterRequest.setPassword("Mike@2002");
        adminRegisterRequest.setRole(Role.ADMIN);
        RegisterResponse adminRegisterResponse = authenticationService.register(adminRegisterRequest);
        assertTrue(adminRegisterResponse.isSuccess());

        LoginRequest adminLoginRequest = new LoginRequest();
        adminLoginRequest.setEmail("mike@gmail.com");
        adminLoginRequest.setPassword("Mike@2002");
        LoginResponse adminLoginResponse = authenticationService.login(adminLoginRequest);
        assertTrue(adminLoginResponse.isSuccess());


        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "mike@gmail.com", null, Collections.singleton(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);


        Book book = new Book();
        book.setTitle("The dramatic world");
        book.setAuthor("Enenche John");
        book.setPublisher("J.A idoko");
        book.setIsbn("43j5674893065");
        book.setYearPublished(2020);
        Book savedBook = adminService.addBook(book);
        assertNotNull(savedBook);

        // Borrow book as user
        BorrowResponse borrowBookRecord = borrowService.borrowBook(savedBook.getIsbn(), "john@gmail.com");
        assertNotNull(borrowBookRecord);
        assertEquals("Book borrowed successfully", borrowBookRecord.getMessage());
        assertTrue(borrowBookRecord.isSuccess());
    }

    @Test
    public void testCannotBorrowAlreadyBorrowedBook() {
        // Register user1
        RegisterRequest user1Request = new RegisterRequest();
        user1Request.setUsername("John_ada");
        user1Request.setEmail("john@gmail.com");
        user1Request.setPassword("John@2002");
        user1Request.setRole(Role.USER);
        authenticationService.register(user1Request);

        // Register user2
        RegisterRequest user2Request = new RegisterRequest();
        user2Request.setUsername("Jane_aa");
        user2Request.setEmail("jane@gmail.com");
        user2Request.setPassword("Jane@2002");
        user2Request.setRole(Role.USER);
        authenticationService.register(user2Request);

        // Register admin
        RegisterRequest adminRequest = new RegisterRequest();
        adminRequest.setUsername("Mike");
        adminRequest.setEmail("mike@gmail.com");
        adminRequest.setPassword("Mike@2002");
        adminRequest.setRole(Role.ADMIN);
        authenticationService.register(adminRequest);

        // Set admin security context
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "mike@gmail.com", null, Collections.singleton(new SimpleGrantedAuthority("ROLE_ADMIN")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        // Add book
        Book book = new Book();
        book.setTitle("Test Book");
        book.setAuthor("Author");
        book.setPublisher("Publisher");
        book.setIsbn("1234567890123");
        book.setYearPublished(2020);
        Book savedBook = adminService.addBook(book);
        assertNotNull(savedBook);

        // Borrow with user1
        BorrowResponse borrow1 = borrowService.borrowBook(savedBook.getIsbn(), "john@gmail.com");
        assertTrue(borrow1.isSuccess());

        // Try borrow with user2
        BorrowResponse borrow2 = borrowService.borrowBook(savedBook.getIsbn(), "jane@gmail.com");
        assertFalse(borrow2.isSuccess());
        assertEquals("Book is not available for borrowing", borrow2.getMessage());
//        assertThrows(IllegalArgumentException.class, () ->
//               );
    }
    @Test
    public void returnBook_test() {
        // Register user
        RegisterRequest userRequest = new RegisterRequest();
        userRequest.setUsername("John_hip");
        userRequest.setEmail("john@gmail.com");
        userRequest.setPassword("John@2002");
        userRequest.setRole(Role.USER);
        authenticationService.register(userRequest);

        // Register admin
        RegisterRequest adminRequest = new RegisterRequest();
        adminRequest.setUsername("Mike");
        adminRequest.setEmail("mike@gmail.com");
        adminRequest.setPassword("Mike@2002");
        adminRequest.setRole(Role.ADMIN);
        authenticationService.register(adminRequest);

        // Set admin security context
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "mike@gmail.com", null, Collections.singleton(new SimpleGrantedAuthority("ROLE_ADMIN")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        // Add book
        Book book = new Book();
        book.setTitle("Test Book");
        book.setAuthor("Author");
        book.setPublisher("Publisher");
        book.setIsbn("1234567890123");
        book.setYearPublished(2020);
        Book savedBook = adminService.addBook(book);
        assertNotNull(savedBook);
        System.out.println(savedBook);

        // Borrow book
        BorrowResponse borrowResponse = borrowService.borrowBook("1234567890123", "john@gmail.com");
        assertTrue(borrowResponse.isSuccess());

        // Return book
        BorrowResponse returnResponse = borrowService.returnBook(savedBook.getIsbn(), "john@gmail.com");
        assertNotNull(returnResponse);
        assertEquals("Book returned successfully", returnResponse.getMessage());
        assertTrue(returnResponse.isSuccess());
        assertEquals("Book returned successfully", returnResponse.getMessage());

        // Verify database
        BorrowBookRecord record = borrowBookRecordRepository.findReturnedBookByBookIsbnAndStatus(savedBook.getIsbn(), BorrowStatus.RETURNED)
                .orElse(null);
        assertNotNull(record);
        assertEquals(BorrowStatus.RETURNED, record.getStatus());
    }
    @Test
    public void testCannotReturnAlreadyReturnedBook() {
        // Register user
        RegisterRequest userRequest = new RegisterRequest();
        userRequest.setUsername("John");
        userRequest.setEmail("john@gmail.com");
        userRequest.setPassword("John@2002");
        userRequest.setRole(Role.USER);
        authenticationService.register(userRequest);

        // Register admin
        RegisterRequest adminRequest = new RegisterRequest();
        adminRequest.setUsername("Mike");
        adminRequest.setEmail("mike@gmail.com");
        adminRequest.setPassword("Mike@2002");
        adminRequest.setRole(Role.ADMIN);
        authenticationService.register(adminRequest);

        // Set admin security context
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "mike@gmail.com", null, Collections.singleton(new SimpleGrantedAuthority("ROLE_ADMIN")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        // Add book
        Book book = new Book();
        book.setTitle("Test Book");
        book.setAuthor("Author");
        book.setPublisher("Publisher");
        book.setIsbn("1234567890123");
        book.setYearPublished(2020);
        Book savedBook = adminService.addBook(book);
        assertNotNull(savedBook);

        // Borrow book
        BorrowResponse borrowResponse = borrowService.borrowBook(savedBook.getIsbn(), "john@gmail.com");
        assertTrue(borrowResponse.isSuccess());

        // Return book
        BorrowResponse returnResponse = borrowService.returnBook(savedBook.getIsbn(), "john@gmail.com");
        assertTrue(returnResponse.isSuccess());

        // Try return again
        BorrowResponse returnRespone2 = borrowService.returnBook(savedBook.getIsbn(), "john@gmail.com");
        assertFalse(returnRespone2.isSuccess());
        assertEquals("Book is not currently borrowed", returnRespone2.getMessage());

    }
}