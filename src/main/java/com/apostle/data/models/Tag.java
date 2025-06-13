package com.apostle.data.models;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "tags")
@Data
public class Tag {
    @Id
    private String id;
    @NotBlank
    private String name;
}
