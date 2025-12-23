package controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.softserve.bookstoreapi.dto.AuthorDTO;
import com.softserve.bookstoreapi.dto.BookDTO;
import com.softserve.bookstoreapi.controller.BookController;
import com.softserve.bookstoreapi.mapper.BookMapper;
import com.softserve.bookstoreapi.model.Book;
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
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver; // Важный импорт!
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BookController Tests")
class BookControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private BookService bookService;

    @Mock
    private BookMapper bookMapper;

    @InjectMocks
    private BookController bookController;

    private BookDTO validBookDto;
    private Book savedBookEntity;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(bookController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();

        validBookDto = new BookDTO(
                "Harry Potter",
                "A magical story",
                "Fantasy",
                "Teen",
                1997,
                "EN",
                List.of(new AuthorDTO("J.K.", "Rowling")),
                BigDecimal.valueOf(20.50),
                100,
                BigDecimal.ZERO,
                300,
                "http://image.url"
        );

        savedBookEntity = new Book();
        savedBookEntity.setId(1L);
        savedBookEntity.setTitle("Harry Potter");
    }

    @Test
    @DisplayName("createBook: Should return 200 and DTO on success")
    void createBook_Success() throws Exception {
        when(bookService.createBook(any(BookDTO.class))).thenReturn(savedBookEntity);
        when(bookMapper.toDto(any(Book.class))).thenReturn(validBookDto);

        mockMvc.perform(post("/api/book")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validBookDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Harry Potter"));
    }

    @Test
    @DisplayName("createBook: Should return 400 when validation fails")
    void createBook_ValidationFail() throws Exception {
        BookDTO invalidDto = new BookDTO(
                null, "Desc", "Genre", "Age", 2020, "EN", List.of(),
                BigDecimal.TEN, 10, BigDecimal.ZERO, 100, "url"
        );

        mockMvc.perform(post("/api/book")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("getBooks: Should return 200 and Page of Books")
    void getBooks_Success() throws Exception {
        List<BookDTO> books = Collections.singletonList(validBookDto);
        Page<BookDTO> bookPage = new PageImpl<>(books, org.springframework.data.domain.PageRequest.of(0, 10), books.size());

        when(bookService.getBooks(isNull(), isNull(), any(Pageable.class)))
                .thenReturn(bookPage);

        mockMvc.perform(get("/api/book")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Harry Potter"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("getBooks: Should filter by params")
    void getBooks_WithParams_Success() throws Exception {
        Page<BookDTO> bookPage = new PageImpl<>(Collections.singletonList(validBookDto), org.springframework.data.domain.PageRequest.of(0, 10), 1);

        when(bookService.getBooks(eq("Fantasy"), eq("Harry"), any(Pageable.class)))
                .thenReturn(bookPage);

        mockMvc.perform(get("/api/book")
                        .param("genreName", "Fantasy")
                        .param("title", "Harry")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Harry Potter"));
    }
}