package com.softserve.bookstoreapi.service.impl;

import com.softserve.bookstoreapi.dto.ReviewRequestDTO;
import com.softserve.bookstoreapi.dto.ReviewResponseDTO;
import com.softserve.bookstoreapi.model.Account;
import com.softserve.bookstoreapi.model.Book;
import com.softserve.bookstoreapi.model.Review;
import com.softserve.bookstoreapi.repository.AccountRepository;
import com.softserve.bookstoreapi.repository.BookRepository;
import com.softserve.bookstoreapi.repository.OrderRepository;
import com.softserve.bookstoreapi.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.softserve.bookstoreapi.model.enums.UserRole;
import org.springframework.security.access.AccessDeniedException;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;
    private final AccountRepository accountRepository;
    private final BookRepository bookRepository;

    @Transactional
    public void deleteReview(Long reviewId, String userEmail) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found with id: " + reviewId));

        Account currentUser = accountRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean isAdmin = currentUser.getRole() == UserRole.ROLE_ADMIN;
        boolean isAuthor = review.getAccount().getId().equals(currentUser.getId());

        if (isAdmin || isAuthor) {
            reviewRepository.delete(review);
        } else {
            throw new AccessDeniedException("You do not have permission to delete this review.");
        }
    }
    @Transactional
    public ReviewResponseDTO addReview(String userEmail, ReviewRequestDTO request) {
        Account account = accountRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Book book = bookRepository.findById(request.bookId())
                .orElseThrow(() -> new RuntimeException("Book not found"));

        boolean hasPurchased = orderRepository.existsPaidOrderForBook(userEmail, book.getId());
        if (!hasPurchased) {
            throw new RuntimeException("You can only review books you have purchased.");
        }

        if (reviewRepository.existsByAccountAndBook(account, book)) {
            throw new RuntimeException("You have already reviewed this book.");
        }

        Review review = new Review();
        review.setAccount(account);
        review.setBook(book);
        review.setRating(request.rating());
        review.setComment(request.comment());

        Review savedReview = reviewRepository.save(review);

        return mapToDto(savedReview);
    }

    private ReviewResponseDTO mapToDto(Review review) {
        return new ReviewResponseDTO(
                review.getId(),
                review.getBook().getId(),
                review.getAccount().getUsername(),
                review.getRating(),
                review.getComment(),
                review.getCreatedAt()
        );
    }
}