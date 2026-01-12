package service;

import com.softserve.bookstoreapi.dto.LanguageDTO;
import com.softserve.bookstoreapi.model.Language;
import com.softserve.bookstoreapi.repository.LanguageRepository;
import com.softserve.bookstoreapi.service.LanguageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LanguageService Tests")
class LanguageServiceTest {

    @Mock
    private LanguageRepository languageRepository;

    @InjectMocks
    private LanguageService languageService;

    @Test
    @DisplayName("saveLanguage: Should correctly map fields and return saved entity")
    void saveLanguage_Success() {

        LanguageDTO dto = new LanguageDTO("FR", "French");

        Language savedLang = new Language();
        savedLang.setId(1L);
        savedLang.setCode("FR");
        savedLang.setName("French");

        when(languageRepository.save(any(Language.class))).thenReturn(savedLang);

        Language result = languageService.saveLanguage(dto);

        assertThat(result.getId()).isEqualTo(1L);

        ArgumentCaptor<Language> captor = ArgumentCaptor.forClass(Language.class);
        verify(languageRepository).save(captor.capture());

        Language captured = captor.getValue();

        assertThat(captured.getCode()).isEqualTo("FR");
        assertThat(captured.getName()).isEqualTo("French");
    }

    @Test
    @DisplayName("saveLanguage: Should throw DataIntegrityViolationException when duplicate exists")
    void saveLanguage_Duplicate_ThrowsException() {
        LanguageDTO dto = new LanguageDTO("EN", "English");

        when(languageRepository.save(any(Language.class)))
                .thenThrow(new DataIntegrityViolationException("Duplicate key value violates unique constraint"));

        assertThatThrownBy(() -> languageService.saveLanguage(dto))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("Duplicate key");

        verify(languageRepository).save(any(Language.class));
    }

    @Test
    @DisplayName("saveLanguage: Should throw NullPointerException when input DTO is null")
    void saveLanguage_NullInput_ThrowsNPE() {
        assertThrows(NullPointerException.class, () -> {
            languageService.saveLanguage(null);
        });
        verify(languageRepository, never()).save(any());
    }

    @Test
    @DisplayName("saveLanguage: Should propagate generic RuntimeException from repository")
    void saveLanguage_DbError_PropagatesException() {
        LanguageDTO dto = new LanguageDTO("DE", "German");

        when(languageRepository.save(any(Language.class)))
                .thenThrow(new RuntimeException("Database unavailable"));

        assertThatThrownBy(() -> languageService.saveLanguage(dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Database unavailable");
    }
}