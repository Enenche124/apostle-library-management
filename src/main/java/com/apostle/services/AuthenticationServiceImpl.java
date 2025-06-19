package com.apostle.services;

import com.apostle.data.models.Admin;
import com.apostle.data.models.Role;
import com.apostle.data.models.User;
import com.apostle.data.repositories.AdminRepository;
import com.apostle.data.repositories.UserRepository;
import com.apostle.dtos.requests.LoginRequest;
import com.apostle.dtos.requests.RegisterRequest;
import com.apostle.dtos.responses.LoginResponse;
import com.apostle.dtos.responses.RegisterResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.apostle.utils.Mapper.*;

@Service
public class AuthenticationServiceImpl implements AuthenticationService{

    private final UserRepository userRepository;
    private final AdminRepository adminRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final Validator validator;
    private final JwtService jwtService;
    private final EmailService emailService;

    @Autowired
    public AuthenticationServiceImpl(UserRepository userRepository,
                                     AdminRepository adminRepository,
                                     BCryptPasswordEncoder bCryptPasswordEncoder,
                                     JwtService jwtService, EmailService emailService) {
        this.userRepository = userRepository;
        this.adminRepository = adminRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.jwtService = jwtService;
        this.emailService = emailService;

        try(ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            this.validator = factory.getValidator();
        }

    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+[a-zA-Z]{2,}$";
        return email != null && email.matches(emailRegex);
    }

    @Override
    public RegisterResponse register(RegisterRequest registerRequest) {
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(registerRequest);
        if (!violations.isEmpty()) {
            String errorMessage = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(", ", "Invalid input: ", "."));
            return new RegisterResponse(false, errorMessage);
        }
        String email = registerRequest.getEmail().toLowerCase().trim();
        Role role = registerRequest.getRole();
        String encodedPassword = bCryptPasswordEncoder.encode(registerRequest.getPassword());
        if (!isValidEmail(email)) {
            return new RegisterResponse(false, "Invalid email");
        }

        boolean emailUsedByAdmin = adminRepository.findAdminByEmail(email).isPresent();
        boolean emailUsedByUser =  userRepository.findUserByEmail(email).isPresent();

        if (emailUsedByAdmin || emailUsedByUser){
            return new RegisterResponse(false, "Email already used");
        }


        switch (role) {
            case USER:
                User user = mapToUser(registerRequest);
                user.setPassword(encodedPassword);
                userRepository.save(user);
                    emailService.sendRegistrationConfirmation(email, user.getUsername());
                return mapToRegisterResponse(true, "User registered successfully");
            case ADMIN:
                Admin admin = mapToAdmin(registerRequest);
                admin.setPassword(encodedPassword);
                adminRepository.save(admin);
                return mapToRegisterResponse(true, "Admin registered successfully");
            default:
                return mapToRegisterResponse(false, "Invalid role");
        }

    }

    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        String email = loginRequest.getEmail().toLowerCase().trim();
        String password = loginRequest.getPassword();

        Optional<User> foundedUser = userRepository.findUserByEmail(email);
        if (foundedUser.isPresent() && bCryptPasswordEncoder.matches(password, foundedUser.get().getPassword())) {
            String token = jwtService.generateToken(email, Role.USER);
            System.out.println("User token: " + token);
            return mapToLoginResponse(foundedUser.get().getUsername(), true, "ROLE_USER", token);
        }

        Optional<Admin> foundedAdmin = adminRepository.findAdminByEmail(email);
        if (foundedAdmin.isPresent() && bCryptPasswordEncoder.matches(password, foundedAdmin.get().getPassword())) {
            String token = jwtService.generateToken(email, Role.ADMIN);
            System.out.println("Admin token: " + token);
            return mapToLoginResponse(foundedAdmin.get().getUsername(), true, "ROLE_ADMIN", token);
        }
        return mapToLoginResponse(null, false, null, null);
    }
}
