package controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.softserve.bookstoreapi.dto.GenreDTO;
import com.softserve.bookstoreapi.controller.GenreController;
import com.softserve.bookstoreapi.mapper.GenreMapper;
import com.softserve.bookstoreapi.model.Genre;
import com.softserve.bookstoreapi.service.GenreService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GenreController Tests")
class GenreControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private GenreService genreService;

    @Mock
    private GenreMapper genreMapper;

    @InjectMocks
    private GenreController genreController;

    private GenreDTO requestDto;
    private GenreDTO responseDto;
    private Genre savedEntity;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(genreController).build();

        requestDto = new GenreDTO(null, "Fantasy", "Books with magical worlds");
        responseDto = new GenreDTO(1L, "Fantasy", "Books with magical worlds");

        savedEntity = new Genre();
        savedEntity.setId(1L);
        savedEntity.setName("Fantasy");
        savedEntity.setDescription("Books with magical worlds");
    }

    @Test
    @DisplayName("Should create Genre successfully")
    void createGenre_Success() throws Exception {
        when(genreService.addGenre(any(GenreDTO.class))).thenReturn(responseDto);

        mockMvc.perform(post("/api/genres")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Fantasy"))
                .andExpect(jsonPath("$.description").value("Books with magical worlds"));
    }

    @Test
    @DisplayName("Should return 400 for missing name")
    void createGenre_MissingName_Returns400() throws Exception {
        GenreDTO invalidDto = new GenreDTO(null, "", "Books with magical worlds");

        mockMvc.perform(post("/api/genres")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 for null DTO")
    void createGenre_NullDto_Returns400() throws Exception {
        mockMvc.perform(post("/api/genres")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

}