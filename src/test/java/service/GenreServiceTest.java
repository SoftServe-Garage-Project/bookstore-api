package service;

import com.softserve.bookstoreapi.DTO.GenreDTO;
import com.softserve.bookstoreapi.model.Genre;
import com.softserve.bookstoreapi.repository.GenreRepository;
import com.softserve.bookstoreapi.service.GenreService;
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
@DisplayName("Tests for GenreService")
class GenreServiceTest {

    @Mock
    private GenreRepository genreRepository;

    @InjectMocks
    private GenreService genreService;

    @Test
    @DisplayName("addGenre: Should successfully save and return the genre")
    void addGenre_Success_ReturnsSavedEntity() {
        GenreDTO dto = new GenreDTO("Fantasy", "Magic books");

        Genre savedGenre = new Genre();
        savedGenre.setId(1L);
        savedGenre.setName("Fantasy");
        savedGenre.setDescription("Magic books");

        when(genreRepository.save(any(Genre.class))).thenReturn(savedGenre);

        Genre result = genreService.addGenre(dto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Fantasy");

        verify(genreRepository, times(1)).save(any(Genre.class));
    }

    @Test
    @DisplayName("addGenre: Should correctly map DTO fields to Entity")
    void addGenre_Success_MappingCorrect() {
        GenreDTO dto = new GenreDTO("Horror", "Scary books");
        when(genreRepository.save(any(Genre.class))).thenReturn(new Genre());

        genreService.addGenre(dto);

        ArgumentCaptor<Genre> genreCaptor = ArgumentCaptor.forClass(Genre.class);
        verify(genreRepository).save(genreCaptor.capture());

        Genre capturedGenre = genreCaptor.getValue();

        assertThat(capturedGenre.getName())
                .as("Name should be mapped correctly")
                .isEqualTo("Horror");

        assertThat(capturedGenre.getDescription())
                .as("Description should be mapped correctly")
                .isEqualTo("Scary books");

        assertThat(capturedGenre.getId()).isNull();
    }


    @Test
    @DisplayName("addGenre: Should propagate exception when Repository fails")
    void addGenre_RepositoryThrowsException() {
        GenreDTO dto = new GenreDTO("Duplicate", "Desc");

        when(genreRepository.save(any(Genre.class)))
                .thenThrow(new DataIntegrityViolationException("Duplicate name"));

        assertThatThrownBy(() -> genreService.addGenre(dto))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessage("Duplicate name");

        verify(genreRepository).save(any(Genre.class));
    }
    @Test
    @DisplayName("addGenre: Should throw NullPointerException when input is null")
    void addGenre_NullInput_ThrowsNPE() {
        assertThrows(NullPointerException.class, () -> {
            genreService.addGenre(null);
        });

        verify(genreRepository, never()).save(any());
    }
}