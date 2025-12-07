package service;

import com.softserve.bookstoreapi.model.AgeGroup;
import com.softserve.bookstoreapi.repository.AgeGroupRepository;
import com.softserve.bookstoreapi.service.AgeGroupService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests for AgeGroupService")
class AgeGroupServiceTest {

    @Mock
    private AgeGroupRepository ageGroupRepository;

    @InjectMocks
    private AgeGroupService ageGroupService;


    @Test
    @DisplayName("findByName: Should return AgeGroup when found")
    void findByName_Found() {
        String searchName = "Teen";
        AgeGroup mockGroup = new AgeGroup();
        mockGroup.setId(1L);
        mockGroup.setName("Teen");

        when(ageGroupRepository.findByNameIgnoreCase(searchName))
                .thenReturn(Optional.of(mockGroup));

        Optional<AgeGroup> result = ageGroupService.findByName(searchName);

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Teen");

        verify(ageGroupRepository).findByNameIgnoreCase(searchName);
    }

    @Test
    @DisplayName("findByName: Should return empty Optional when not found")
    void findByName_NotFound() {
        String searchName = "Unknown";
        when(ageGroupRepository.findByNameIgnoreCase(searchName))
                .thenReturn(Optional.empty());

        Optional<AgeGroup> result = ageGroupService.findByName(searchName);

        assertThat(result).isEmpty(); // Check Optional is empty
        verify(ageGroupRepository).findByNameIgnoreCase(searchName);
    }

    @Test
    @DisplayName("save: Should save and return the AgeGroup entity")
    void save_Success() {
        AgeGroup inputGroup = new AgeGroup();
        inputGroup.setName("Kids");

        AgeGroup savedGroup = new AgeGroup();
        savedGroup.setId(10L);
        savedGroup.setName("Kids");

        when(ageGroupRepository.save(any(AgeGroup.class))).thenReturn(savedGroup);

        AgeGroup result = ageGroupService.save(inputGroup);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getName()).isEqualTo("Kids");

        verify(ageGroupRepository).save(inputGroup);
    }

    @Test
    @DisplayName("save: Should verify exact object passed to repository")
    void save_VerifyArgumentPassed() {
        AgeGroup group = new AgeGroup();
        group.setName("Adults");

        ageGroupService.save(group);

        ArgumentCaptor<AgeGroup> captor = ArgumentCaptor.forClass(AgeGroup.class);
        verify(ageGroupRepository).save(captor.capture());

        assertThat(captor.getValue().getName()).isEqualTo("Adults");
    }
}