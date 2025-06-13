package com.apostle.data.repositories;

import com.apostle.data.models.Admin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminRepository extends MongoRepository<Admin,String> {

    Optional<Admin> findAdminByEmail(@Email(message = "Email is invalid") @NotBlank String email);
}
