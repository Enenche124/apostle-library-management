package com.apostle.services;

import com.apostle.dtos.requests.LoginRequest;
import com.apostle.dtos.requests.RegisterRequest;
import com.apostle.dtos.responses.LoginResponse;
import com.apostle.dtos.responses.RegisterResponse;

public interface AuthenticationService {
    RegisterResponse register(RegisterRequest registerRequest);
    LoginResponse login(LoginRequest loginRequest);
}
