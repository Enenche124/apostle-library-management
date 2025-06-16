package com.apostle.services;

import com.apostle.Main;
import com.apostle.data.models.Role;
import com.apostle.data.repositories.AdminRepository;
import com.apostle.data.repositories.UserRepository;
import com.apostle.dtos.requests.LoginRequest;
import com.apostle.dtos.requests.RegisterRequest;
import com.apostle.dtos.responses.LoginResponse;
import com.apostle.dtos.responses.RegisterResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = {Main.class })
public class AuthenticationServiceImplTest {

    @Autowired
    private AuthenticationServiceImpl authenticationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AdminRepository adminRepository;

    @BeforeEach
    public void cleanDb(){
        userRepository.deleteAll();
        adminRepository.deleteAll();
    }
    @Test
    public void registerUser_test(){
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("John");
        registerRequest.setEmail("jack@gmai.om");
        registerRequest.setPassword("John@2002");
        registerRequest.setRole(Role.USER);

        RegisterResponse registerResponse = authenticationService.register(registerRequest);
        assertTrue(registerResponse.isSuccess());
    }

    @Test
    public void registerUserFailsForEmptyUsername_test(){
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername(" ");
        registerRequest.setEmail("jack@gmai.om");
        registerRequest.setPassword("John@2002");
        registerRequest.setRole(Role.USER);
        RegisterResponse registerResponse = authenticationService.register(registerRequest);
        assertFalse(registerResponse.isSuccess());
    }
    @Test
    public void registerUserFailsForEmptyEmail_test(){
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("John");
        registerRequest.setEmail(" ");
        registerRequest.setPassword("John@2002");
        registerRequest.setRole(Role.USER);
        RegisterResponse registerResponse = authenticationService.register(registerRequest);
        assertFalse(registerResponse.isSuccess());
    }

    @Test
    public void registerFailsForEmptyPassword_test(){
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("John");
        registerRequest.setEmail("John@2002");
        registerRequest.setPassword(" ");
        registerRequest.setRole(Role.USER);
        RegisterResponse registerResponse = authenticationService.register(registerRequest);
        assertFalse(registerResponse.isSuccess());
    }
    @Test
    public void registerFailsForEmptyRole_test(){
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("John");
        registerRequest.setEmail("John@2002");
        registerRequest.setPassword("John@2002");
        registerRequest.setRole(null);
        RegisterResponse registerResponse = authenticationService.register(registerRequest);
        assertFalse(registerResponse.isSuccess());
    }
    @Test
    public void registerFailsForInvalidEmail_test(){
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("John");
        registerRequest.setEmail("john@gmail");
        registerRequest.setPassword("John@2002");
        registerRequest.setRole(Role.USER);
        RegisterResponse registerResponse = authenticationService.register(registerRequest);
        assertFalse(registerResponse.isSuccess());
    }

    @Test
    public void registerAmin_test(){
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail("admin@gmail.com");
        registerRequest.setPassword("Password@111");
        registerRequest.setUsername("admin");
        registerRequest.setRole(Role.ADMIN);
        RegisterResponse registerResponse = authenticationService.register(registerRequest);
        assertTrue(registerResponse.isSuccess());
    }

    @Test
    public void loginUser_test(){
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
        assertFalse(loginResponse.getToken().isEmpty());
        assertEquals("USER", loginResponse.getRole());
        assertEquals("John", loginResponse.getUsername());
    }

    @Test
    public void loginAdmin_test(){
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("John");
        registerRequest.setEmail("mike@gmail.com");
        registerRequest.setPassword("Mike@2002");
        registerRequest.setRole(Role.ADMIN);
        RegisterResponse registerResponse = authenticationService.register(registerRequest);
        assertTrue(registerResponse.isSuccess());

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("mike@gmail.com");
        loginRequest.setPassword("Mike@2002");
        LoginResponse loginResponse = authenticationService.login(loginRequest);
        assertTrue(loginResponse.isSuccess());
        assertFalse(loginResponse.getToken().isEmpty());
        assertEquals("ADMIN", loginResponse.getRole());
        assertEquals("John", loginResponse.getUsername());

    }

}