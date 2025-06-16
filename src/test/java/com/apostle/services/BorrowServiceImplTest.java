package com.apostle.services;

import com.apostle.Main;
import com.apostle.data.models.Book;
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

@SpringBootTest(classes = {Main.class})
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
//    @Test
//    public void testCannotBorrowAlreadyBorrowedBook(){
//        String isbn = "1234567520";
//        String userId = "user123";
//        String userId2 = "user424";
//
//        borrowService.borrowBook(isbn, userId);
//        assertThrows(IllegalArgumentException.class, () -> borrowService.borrowBook(isbn, userId2) );
//    }
//
//    @Test
//    public void returnBook_test(){
//        String isbn = "1434567820";
//        String userId = "user123";
//        BorrowBookRecord borrowBookRecord = borrowService.borrowBook(isbn, userId);
//        assertNotNull(borrowBookRecord);
//        BorrowBookRecord returnedBook = borrowService.returnBook(borrowBookRecord.getId());
//        assertNotNull(returnedBook);
//        assertEquals(BorrowStatus.RETURNED, returnedBook.getStatus());
//    }
//
//    @Test
//    public void testCannotReturnAlreadyReturnedBook(){
//        String isbn = "1434567820";
//        String userId = "user123";
//        BorrowBookRecord borrowBookRecord = borrowService.borrowBook(isbn, userId);
//        assertNotNull(borrowBookRecord);
//        BorrowBookRecord returnedBook = borrowService.returnBook(borrowBookRecord.getId());
//        assertNotNull(returnedBook);
//        assertThrows(IllegalArgumentException.class, () -> borrowService.returnBook(returnedBook.getId()) );
//    }
}