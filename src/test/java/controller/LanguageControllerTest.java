package controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.softserve.bookstoreapi.DTO.LanguageDTO;
import com.softserve.bookstoreapi.controller.LanguageController;
import com.softserve.bookstoreapi.mapper.LanguageMapper;
import com.softserve.bookstoreapi.model.Language;
import com.softserve.bookstoreapi.service.LanguageService;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LanguageController Tests")
class LanguageControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private LanguageService languageService;

    @Mock
    private LanguageMapper languageMapper;

    @InjectMocks
    private LanguageController languageController;

    private LanguageDTO validDto;
    private Language savedEntity;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(languageController).build();

        validDto = new LanguageDTO("UA", "Ukrainian");

        savedEntity = new Language();
        savedEntity.setId(1L);
        savedEntity.setCode("UA");
        savedEntity.setName("Ukrainian");
    }

    @Test
    @DisplayName("Should save Language successfully and return 200")
    void saveLanguage_Success() throws Exception {
        when(languageService.saveLanguage(any(LanguageDTO.class))).thenReturn(savedEntity);
        when(languageMapper.toDto(any(Language.class))).thenReturn(validDto);

        mockMvc.perform(post("/api/languages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("UA"))
                .andExpect(jsonPath("$.name").value("Ukrainian"));

        verify(languageService).saveLanguage(any(LanguageDTO.class));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when code is missing")
    void saveLanguage_MissingCode_Returns400() throws Exception {
        LanguageDTO invalidDto = new LanguageDTO("", "Ukrainian");

        mockMvc.perform(post("/api/languages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 Bad Request when name is missing")
    void saveLanguage_MissingName_Returns400() throws Exception {
        LanguageDTO invalidDto = new LanguageDTO("UA", null);

        mockMvc.perform(post("/api/languages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }
}