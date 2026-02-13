package com.softserve.bookstoreapi.controller;

import com.softserve.bookstoreapi.dto.ReviewRequestDTO;
import com.softserve.bookstoreapi.dto.ReviewResponseDTO;
import com.softserve.bookstoreapi.service.impl.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ReviewResponseDTO> addReview(@Valid @RequestBody ReviewRequestDTO request, Principal principal)
    {
        ReviewResponseDTO response = reviewService.addReview(principal.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteReview(@PathVariable Long id, Principal principal) {
        reviewService.deleteReview(id, principal.getName());
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/{bookId}")
    public ResponseEntity<Page<ReviewResponseDTO>> getBookReviews(@PathVariable Long bookId,@PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<ReviewResponseDTO> reviews = reviewService.getReviewsByBook(bookId, pageable);
        return ResponseEntity.ok(reviews);
    }
}