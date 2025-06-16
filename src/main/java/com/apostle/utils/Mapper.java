package com.apostle.utils;

import com.apostle.data.models.Admin;
import com.apostle.data.models.BorrowBookRecord;
import com.apostle.data.models.User;
import com.apostle.dtos.requests.RegisterRequest;
import com.apostle.dtos.responses.BorrowResponse;
import com.apostle.dtos.responses.LoginResponse;
import com.apostle.dtos.responses.RegisterResponse;

public class Mapper {

    public static User mapToUser(RegisterRequest registerRequest){
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setPassword(registerRequest.getPassword());
        user.setEmail(registerRequest.getEmail().toLowerCase().trim());
        return user;
    }

    public static Admin mapToAdmin(RegisterRequest registerRequest){
        Admin admin = new Admin();
        admin.setEmail(registerRequest.getEmail());
        admin.setPassword(registerRequest.getPassword());
        admin.setUsername(registerRequest.getUsername());
        return admin;
    }

    public static RegisterResponse mapToRegisterResponse(boolean success, String message){
        return new RegisterResponse(success,message);
    }
    public static LoginResponse mapToLoginResponse(String username, boolean success, String role, String token){
        return new LoginResponse(username,success,role,token);
    }

    public static BorrowResponse mapToBorrowResponse(BorrowBookRecord borrowRecord, String message, boolean success) {
        return new BorrowResponse(
                message,
                success,
                borrowRecord.getId(),
                borrowRecord.getBookIsbn(),
                borrowRecord.getBorrower(),
                borrowRecord.getBorrowDate(),
                borrowRecord.getDueDate(),
                borrowRecord.getStatus(),
                borrowRecord.getFineAmount()
        );
    }

    public static BorrowResponse mapToErrorResponse(String message){
        return new BorrowResponse(
                message,
                false,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

}
