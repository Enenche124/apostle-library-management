package com.apostle.data.models;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Document(collection = "books")
public class Book {
    @Id
//    @NotBlank(message = "ID is required")
    private String id;
    @NotBlank(message = "ISBN is required")
    private String isbn;
//    @NotBlank(message = "Title is required")
    private String title;
//    @NotBlank(message = "Author is required")
    private String author;
//    @NotBlank(message = "Publisher is required")
    private String publisher;
//    @NotNull(message = "Published year is required")
    private int yearPublished;
    private String category;
    private List<String> tags;
    private String imageUrl;
}
