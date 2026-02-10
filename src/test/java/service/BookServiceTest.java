package service;

import com.softserve.bookstoreapi.dto.AuthorDTO;
import com.softserve.bookstoreapi.dto.BookDTO;
import com.softserve.bookstoreapi.mapper.BookMapper;
import com.softserve.bookstoreapi.model.*;
import com.softserve.bookstoreapi.repository.*;
import com.softserve.bookstoreapi.service.BookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests for BookService")
class BookServiceTest {

    @Mock private BookRepository bookRepository;
    @Mock private GenreRepository genreRepository;
    @Mock private AgeGroupRepository ageGroupRepository;
    @Mock private LanguageRepository languageRepository;
    @Mock private AuthorRepository authorRepository;
    @Mock private BookMapper bookMapper;

    @InjectMocks
    private BookService bookService;

    private BookDTO validRequest;
    private Genre mockGenre;
    private AgeGroup mockAgeGroup;
    private Language mockLanguage;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        validRequest = new BookDTO(
                null,"Harry Potter", "Magic", "Fantasy", "Teen", 1997, "EN",
                List.of(new AuthorDTO("J.K.", "Rowling")),
                BigDecimal.valueOf(20.0), 10, BigDecimal.ZERO, 300, "url"
        );

        mockGenre = new Genre(); mockGenre.setId(1L); mockGenre.setName("Fantasy");
        mockAgeGroup = new AgeGroup(); mockAgeGroup.setId(2L); mockAgeGroup.setName("Teen");
        mockLanguage = new Language(); mockLanguage.setId(3L); mockLanguage.setCode("EN");

        pageable = PageRequest.of(0, 10);
    }


    //createBook
    @Test
    @DisplayName("createBook: Success - Should find dependencies, save authors and book")
    void createBook_Success() {
        when(genreRepository.findByNameIgnoreCase("Fantasy")).thenReturn(Optional.of(mockGenre));
        when(ageGroupRepository.findByNameIgnoreCase("Teen")).thenReturn(Optional.of(mockAgeGroup));
        when(languageRepository.findByCodeIgnoreCase("EN")).thenReturn(Optional.of(mockLanguage));

        when(authorRepository.save(any(Author.class))).thenAnswer(invocation -> invocation.getArgument(0));

        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> {
            Book b = invocation.getArgument(0);
            b.setId(100L);
            return b;
        });

        Book result = bookService.createBook(validRequest);

        assertThat(result.getId()).isEqualTo(100L);
        assertThat(result.getTitle()).isEqualTo("Harry Potter");

        assertThat(result.getGenre()).isEqualTo(mockGenre);
        assertThat(result.getLanguage()).isEqualTo(mockLanguage);
        assertThat(result.getAgeGroup()).isEqualTo(mockAgeGroup);

        verify(authorRepository, times(1)).save(any(Author.class));
        assertThat(result.getAuthors()).hasSize(1);
        assertThat(result.getAuthors().get(0).getFirstName()).isEqualTo("J.K.");
    }

    @Test
    @DisplayName("createBook: Fail - Should throw Exception when Genre not found")
    void createBook_GenreNotFound() {
        when(languageRepository.findByCodeIgnoreCase("EN")).thenReturn(Optional.of(mockLanguage));

        when(bookRepository.findByTitleIgnoreCaseAndPublishedYearAndLanguage(any(), any(), any()))
                .thenReturn(java.util.Collections.emptyList());
        when(genreRepository.findByNameIgnoreCase("Fantasy")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> bookService.createBook(validRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Genre not found");
        verify(bookRepository, never()).save(any());
    }

    @Test
    @DisplayName("createBook: Fail - Should throw Exception when AgeGroup not found")
    void createBook_AgeGroupNotFound() {
        when(languageRepository.findByCodeIgnoreCase("EN")).thenReturn(Optional.of(mockLanguage));
        when(bookRepository.findByTitleIgnoreCaseAndPublishedYearAndLanguage(any(), any(), any()))
                .thenReturn(java.util.Collections.emptyList());
        when(genreRepository.findByNameIgnoreCase("Fantasy")).thenReturn(Optional.of(mockGenre));

        when(ageGroupRepository.findByNameIgnoreCase("Teen")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> bookService.createBook(validRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Age group not found");

        verify(bookRepository, never()).save(any());
    }

    @Test
    @DisplayName("createBook: Fail - Should throw Exception when Language not found")
    void createBook_LanguageNotFound() {
        when(languageRepository.findByCodeIgnoreCase("EN")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.createBook(validRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Language not found");

        verify(bookRepository, never()).findByTitleIgnoreCaseAndPublishedYearAndLanguage(any(), any(), any());
    }

    @Test
    @DisplayName("getBooks: Filter by Genre only (Success)")
    void getBooks_ByGenre_Success() {
        String genreInput = " Fantasy ";
        when(genreRepository.findByNameIgnoreCase("Fantasy")).thenReturn(Optional.of(mockGenre));

        Page<Book> mockPage = new PageImpl<>(List.of(new Book()));
        when(bookRepository.findByGenreId(mockGenre.getId(), pageable)).thenReturn(mockPage);

        Page<BookDTO> result = bookService.getBooks(genreInput, null, pageable);

        verify(genreRepository).findByNameIgnoreCase("Fantasy"); // Проверяем, что пробелы обрезались
        verify(bookRepository).findByGenreId(mockGenre.getId(), pageable);
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("getBooks: Filter by Genre only (Genre Not Found -> Empty Page)")
    void getBooks_ByGenre_NotFound() {
        when(genreRepository.findByNameIgnoreCase("Unknown")).thenReturn(Optional.empty());

        Page<BookDTO> result = bookService.getBooks("Unknown", "", pageable);

        assertThat(result).isEmpty();
        verify(bookRepository, never()).findByGenreId(any(), any());
    }

    @Test
    @DisplayName("getBooks: Filter by Title only")
    void getBooks_ByTitle() {
        String titleInput = " Harry ";
        Page<Book> mockPage = new PageImpl<>(List.of(new Book()));
        when(bookRepository.findByTitleContainingIgnoreCase("Harry", pageable)).thenReturn(mockPage);

        Page<BookDTO> result = bookService.getBooks(null, titleInput, pageable);

        verify(bookRepository).findByTitleContainingIgnoreCase("Harry", pageable);
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("getBooks: Filter by Both (Success)")
    void getBooks_ByBoth_Success() {
        when(genreRepository.findByNameIgnoreCase("Fantasy")).thenReturn(Optional.of(mockGenre));

        Page<Book> mockPage = new PageImpl<>(List.of(new Book()));
        when(bookRepository.findByTitleContainingIgnoreCaseAndGenreId("Harry", mockGenre.getId(), pageable))
                .thenReturn(mockPage);

        Page<BookDTO> result = bookService.getBooks("Fantasy", "Harry", pageable);

        verify(bookRepository).findByTitleContainingIgnoreCaseAndGenreId("Harry", mockGenre.getId(), pageable);
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("getBooks: Filter by Both (Genre Not Found -> Empty Page)")
    void getBooks_ByBoth_GenreNotFound() {
        when(genreRepository.findByNameIgnoreCase("NonExistent")).thenReturn(Optional.empty());

        Page<BookDTO> result = bookService.getBooks("NonExistent", "Harry", pageable);

        assertThat(result).isEmpty();
        verify(bookRepository, never()).findByTitleContainingIgnoreCaseAndGenreId(any(), any(), any());
    }

    @Test
    @DisplayName("getBooks: Filter by Both (Found Genre but No Books match logic -> Empty Page)")
    void getBooks_ByBoth_NoBooksMatch() {
        when(genreRepository.findByNameIgnoreCase("Fantasy")).thenReturn(Optional.of(mockGenre));
        when(bookRepository.findByTitleContainingIgnoreCaseAndGenreId("Harry", mockGenre.getId(), pageable))
                .thenReturn(Page.empty(pageable));

        Page<BookDTO> result = bookService.getBooks("Fantasy", "Harry", pageable);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getBooks: No filters (Return All)")
    void getBooks_NoFilters() {
        Page<Book> mockPage = new PageImpl<>(List.of(new Book()));
        when(bookRepository.findAll(pageable)).thenReturn(mockPage);

        Page<BookDTO> result = bookService.getBooks(null, null, pageable);

        verify(bookRepository).findAll(pageable);
        verify(bookMapper, atLeastOnce()).toDto(any());
    }

    @Test
    @DisplayName("getBooks: Blank strings (Return All)")
    void getBooks_BlankStrings() {
        Page<Book> mockPage = new PageImpl<>(List.of(new Book()));
        when(bookRepository.findAll(pageable)).thenReturn(mockPage);

        Page<BookDTO> result = bookService.getBooks("   ", "", pageable);

        verify(bookRepository).findAll(pageable);
    }
    @Test
    @DisplayName("updateBook: Success - Should update fields and return updated book")
    void updateBook_Success() {
        Long bookId = 100L;
        Book existingBook = new Book();
        existingBook.setId(bookId);
        existingBook.setTitle("Old Title");

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(existingBook));

        when(genreRepository.findByNameIgnoreCase("Fantasy")).thenReturn(Optional.of(mockGenre));
        when(ageGroupRepository.findByNameIgnoreCase("Teen")).thenReturn(Optional.of(mockAgeGroup));
        when(languageRepository.findByCodeIgnoreCase("EN")).thenReturn(Optional.of(mockLanguage));
        when(authorRepository.findByFirstNameIgnoreCaseAndLastNameIgnoreCase(anyString(), anyString())).thenReturn(Optional.empty());
        when(authorRepository.save(any(Author.class))).thenAnswer(inv -> inv.getArgument(0));

        when(bookRepository.save(any(Book.class))).thenAnswer(inv -> inv.getArgument(0));

        Book updatedBook = bookService.updateBook(bookId, validRequest);

        assertThat(updatedBook.getTitle()).isEqualTo("Harry Potter");
        verify(bookRepository).save(existingBook);
    }

    @Test
    @DisplayName("updateBook: Fail - Book not found")
    void updateBook_NotFound() {
        when(bookRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.updateBook(999L, validRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Book not found with id: 999");

        verify(bookRepository, never()).save(any());
    }

    @Test
    @DisplayName("deleteBook: Success - Should delete existing book")
    void deleteBook_Success() {
        Long bookId = 100L;
        when(bookRepository.existsById(bookId)).thenReturn(true);

        bookService.deleteBook(bookId);

        verify(bookRepository).deleteById(bookId);
    }

    @Test
    @DisplayName("deleteBook: Fail - Book not found")
    void deleteBook_NotFound() {
        Long bookId = 999L;
        when(bookRepository.existsById(bookId)).thenReturn(false);

        assertThatThrownBy(() -> bookService.deleteBook(bookId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Book not found with id: 999");

        verify(bookRepository, never()).deleteById(any());
    }
}