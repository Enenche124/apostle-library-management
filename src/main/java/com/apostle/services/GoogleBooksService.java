package com.apostle.services;

import com.apostle.data.models.Book;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Service
public class GoogleBooksService {
    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${google.books.api.key}")
    private String apiKey;

    public GoogleBooksService(WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
        this.webClient = webClientBuilder.baseUrl("https://www.googleapis.com/books/v1").build();
        this.objectMapper = objectMapper;
    }

    public Mono<List<Book>> searchBooks(String query) {
        if (query == null || query.trim().isEmpty()) {
            return Mono.error(new IllegalArgumentException("Search query cannot be null or empty"));
        }

        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/volumes")
                        .queryParam("q", query.trim())
                        .queryParam("maxResults", 40)
                        .queryParam("key", apiKey)
                        .build())
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        response -> Mono.error(new RuntimeException("API call failed with status: " + response.statusCode())))
                .bodyToMono(String.class)
                .map(this::mapToBooks)
                .onErrorResume(e -> Mono.error(new RuntimeException("Error searching books: " + e.getMessage())));
    }

    public Mono<Book> fetchBookByIsbn(String isbn) {
        if (isbn == null || isbn.trim().isEmpty()) {
            return Mono.error(new IllegalArgumentException("ISBN cannot be null or empty"));
        }

        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/volumes")
                        .queryParam("q", "isbn:" + isbn.trim())
                        .queryParam("key", apiKey)
                        .build())
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        response -> Mono.error(new RuntimeException("API call failed with status: " + response.statusCode())))
                .bodyToMono(String.class)
                .map(this::mapToBook)
                .onErrorResume(e -> Mono.error(new RuntimeException("Error searching books: " + e.getMessage())))
                .flatMap(book -> book != null ?
                        Mono.just(book) :
                        Mono.error(new RuntimeException("Book not found for ISBN: " + isbn)));
    }

    private List<Book> mapToBooks(String response) {
        List<Book> books = new ArrayList<>();
        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode items = root.path("items");
            if (!items.isMissingNode()) {
                for (JsonNode item : items) {
                    Book book = mapJsonToBook(item);
                    if (book != null) {
                        books.add(book);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error parsing book response: " + e.getMessage());
        }
        return books;
    }


    private Book mapToBook(String response) {
        System.out.println("oogle Books API Response: " + response);
        try {
            JsonNode root = objectMapper.readTree(response);
            if (root.path("totalItems").asInt() == 0) {
                System.err.println("No items found in Google Books response for ISBN");
                return null;
            }
            JsonNode items = root.path("items");
            if (items.isArray() && !items.isEmpty()) {
                return mapJsonToBook(items.get(0));
            }
            System.err.println("No valid items in Google Books response");
            return null;
        } catch (Exception e) {
            System.err.println("Error parsing Google Books response: " + e.getMessage());
            return null;
        }
    }

    private Book mapJsonToBook(JsonNode item) {
        Book book = new Book();
        JsonNode volumeInfo = item.path("volumeInfo");
        String title = volumeInfo.path("title").asText(null);
        String author = volumeInfo.path("authors").isArray() && !volumeInfo.path("authors").isEmpty()
                ? volumeInfo.path("authors").get(0).asText(null) : null;
        String publisher = volumeInfo.path("publisher").asText(null);
        int yearPublished = 0;
        try {
            String publishedDate = volumeInfo.path("publishedDate").asText(null);
            if (publishedDate != null && publishedDate.length() >= 4) {
                yearPublished = Integer.parseInt(publishedDate.substring(0, 4));
            }
        } catch (NumberFormatException e) {
            System.err.println("Invalid published date format: " + e.getMessage());
        }
        String isbn = null;
        JsonNode identifiers = volumeInfo.path("industryIdentifiers");
        for (JsonNode id : identifiers) {
            if ("ISBN_13".equals(id.path("type").asText()) || "ISBN_10".equals(id.path("type").asText())) {
                isbn = id.path("identifier").asText(null);
                break;
            }
        }
        String imageLinks =  getImageUrl(volumeInfo, isbn);

        String category = null;
        List<String> tags = new ArrayList<>();
        JsonNode categories = volumeInfo.path("categories");
        if (categories.isArray()) {
        for (JsonNode cat : categories) {
        String catText = cat.asText(null);
        if (catText != null && !catText.isBlank()) {
            tags.add(catText);
            if (category == null) {
                category = catText;             }
        }
        }
        }

        book.setIsbn(isbn);
        book.setTitle(title);
        book.setAuthor(author);
        book.setPublisher(publisher);
        book.setYearPublished(yearPublished);
        book.setImageUrl(imageLinks);
        book.setCategory(category);
        book.setTags(tags);

        if (title == null || title.isBlank()) {
            System.err.println("Skipping book with missing title");
            return null;
        }
        return book;
    }
    private String getImageUrl(JsonNode volumeInfo, String isbn) {
        String googleCover = volumeInfo.path("imageLinks").path("thumbnail").asText(null);
        if (googleCover != null) {
            return googleCover;
        }

        if (isbn != null) {
            return "https://covers.openlibrary.org/b/isbn/" + isbn + "-M.jpg";
        }
        return null;
    }
}