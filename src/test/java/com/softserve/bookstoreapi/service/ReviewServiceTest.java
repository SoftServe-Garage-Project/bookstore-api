package com.softserve.bookstoreapi.service;

import com.softserve.bookstoreapi.dto.ReviewRequestDTO;
import com.softserve.bookstoreapi.dto.ReviewResponseDTO;
import com.softserve.bookstoreapi.model.Account;
import com.softserve.bookstoreapi.model.Book;
import com.softserve.bookstoreapi.model.Review;
import com.softserve.bookstoreapi.model.enums.UserRole;
import com.softserve.bookstoreapi.repository.AccountRepository;
import com.softserve.bookstoreapi.repository.BookRepository;
import com.softserve.bookstoreapi.repository.OrderRepository;
import com.softserve.bookstoreapi.repository.ReviewRepository;
import com.softserve.bookstoreapi.service.impl.ReviewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock private ReviewRepository reviewRepository;
    @Mock private OrderRepository orderRepository;
    @Mock private AccountRepository accountRepository;
    @Mock private BookRepository bookRepository;

    @InjectMocks
    private ReviewService reviewService;

    private final String EMAIL = "test@user.com";
    private Account mockAccount;
    private Book mockBook;
    private ReviewRequestDTO validRequest;

    @BeforeEach
    void setUp() {
        mockAccount = new Account();
        mockAccount.setId(1L);
        mockAccount.setEmail(EMAIL);
        mockAccount.setUsername("John");

        mockBook = new Book();
        mockBook.setId(10L);
        mockBook.setTitle("Java Guide");

        validRequest = new ReviewRequestDTO(10L, 5, "Great book!");
    }

    @Test
    @DisplayName("addReview: Success")
    void addReview_Success() {
        when(accountRepository.findByEmail(EMAIL)).thenReturn(Optional.of(mockAccount));
        when(bookRepository.findById(10L)).thenReturn(Optional.of(mockBook));
        when(orderRepository.existsPaidOrderForBook(EMAIL, 10L)).thenReturn(true);
        when(reviewRepository.existsByAccountAndBook(mockAccount, mockBook)).thenReturn(false);

        when(reviewRepository.save(any(Review.class))).thenAnswer(inv -> {
            Review r = inv.getArgument(0);
            r.setId(99L);
            r.setCreatedAt(LocalDateTime.now());
            return r;
        });

        ReviewResponseDTO result = reviewService.addReview(EMAIL, validRequest);

        assertThat(result.id()).isEqualTo(99L);
        assertThat(result.comment()).isEqualTo("Great book!");
        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    @DisplayName("addReview: Fail - Not Purchased")
    void addReview_NotPurchased() {
        when(accountRepository.findByEmail(EMAIL)).thenReturn(Optional.of(mockAccount));
        when(bookRepository.findById(10L)).thenReturn(Optional.of(mockBook));

        when(orderRepository.existsPaidOrderForBook(EMAIL, 10L)).thenReturn(false);

        assertThatThrownBy(() -> reviewService.addReview(EMAIL, validRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("You can only review books you have purchased");

        verify(reviewRepository, never()).save(any());
    }

    @Test
    @DisplayName("addReview: Fail - Already Reviewed")
    void addReview_Duplicate() {
        when(accountRepository.findByEmail(EMAIL)).thenReturn(Optional.of(mockAccount));
        when(bookRepository.findById(10L)).thenReturn(Optional.of(mockBook));
        when(orderRepository.existsPaidOrderForBook(EMAIL, 10L)).thenReturn(true);

        when(reviewRepository.existsByAccountAndBook(mockAccount, mockBook)).thenReturn(true);

        assertThatThrownBy(() -> reviewService.addReview(EMAIL, validRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("You have already reviewed this book");

        verify(reviewRepository, never()).save(any());
    }

    @Test
    @DisplayName("addReview: Fail - Book Not Found")
    void addReview_BookNotFound() {
        when(accountRepository.findByEmail(EMAIL)).thenReturn(Optional.of(mockAccount));
        when(bookRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.addReview(EMAIL, validRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Book not found");
    }
    @Test
    @DisplayName("deleteReview: Success - Author deletes their own review")
    void deleteReview_ByAuthor_Success() {
        Long reviewId = 100L;
        Review review = new Review();
        review.setId(reviewId);
        review.setAccount(mockAccount);

        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
        when(accountRepository.findByEmail(EMAIL)).thenReturn(Optional.of(mockAccount));

        reviewService.deleteReview(reviewId, EMAIL);

        verify(reviewRepository).delete(review);
    }

    @Test
    @DisplayName("deleteReview: Success - Admin deletes user's review")
    void deleteReview_ByAdmin_Success() {
        Long reviewId = 100L;

        Account author = new Account();
        author.setId(2L);

        Review review = new Review();
        review.setId(reviewId);
        review.setAccount(author);

        Account admin = new Account();
        admin.setId(1L);
        admin.setEmail("admin@test.com");
        admin.setRole(UserRole.ROLE_ADMIN);

        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
        when(accountRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(admin));

        reviewService.deleteReview(reviewId, "admin@test.com");

        verify(reviewRepository).delete(review);
    }

    @Test
    @DisplayName("deleteReview: Fail - Stranger tries to delete review (403)")
    void deleteReview_ByStranger_Forbidden() {
        Long reviewId = 100L;

        Account author = new Account();
        author.setId(2L);

        Review review = new Review();
        review.setId(reviewId);
        review.setAccount(author);

        mockAccount.setId(1L);
        mockAccount.setRole(UserRole.ROLE_CUSTOMER);

        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
        when(accountRepository.findByEmail(EMAIL)).thenReturn(Optional.of(mockAccount));

        assertThatThrownBy(() -> reviewService.deleteReview(reviewId, EMAIL))
                .isInstanceOf(org.springframework.security.access.AccessDeniedException.class)
                .hasMessage("You do not have permission to delete this review.");

        verify(reviewRepository, never()).delete(any());
    }

    @Test
    @DisplayName("deleteReview: Fail - Review Not Found")
    void deleteReview_NotFound() {
        when(reviewRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.deleteReview(999L, EMAIL))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Review not found");
    }
}