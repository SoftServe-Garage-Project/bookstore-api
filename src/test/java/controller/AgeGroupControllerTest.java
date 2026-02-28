package controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.softserve.bookstoreapi.dto.AgeGroupDTO;
import com.softserve.bookstoreapi.controller.AgeGroupController;
import com.softserve.bookstoreapi.mapper.AgeGroupMapper;
import com.softserve.bookstoreapi.model.AgeGroup;
import com.softserve.bookstoreapi.service.AgeGroupService;
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
@DisplayName("AgeGroupController Tests")
class AgeGroupControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private AgeGroupService ageGroupService;

    @Mock
    private AgeGroupMapper ageGroupMapper;

    @InjectMocks
    private AgeGroupController ageGroupController;

    private AgeGroupDTO requestDto;
    private AgeGroupDTO responseDto;
    private AgeGroup savedEntity;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(ageGroupController).build();

        requestDto = new AgeGroupDTO(
                null,
                "Teenagers",
                "Group for 13-19 years old",
                13,
                19
        );

        responseDto = new AgeGroupDTO(
                1L,
                "Teenagers",
                "Group for 13-19 years old",
                13,
                19
        );

        savedEntity = new AgeGroup();
        savedEntity.setId(1L);
        savedEntity.setName("Teenagers");
        savedEntity.setDescription("Group for 13-19 years old");
        savedEntity.setMinAge(13);
        savedEntity.setMaxAge(19);
    }

    @Test
    @DisplayName("Should create AgeGroup successfully")
    void create_Success() throws Exception {
        when(ageGroupMapper.toEntity(any(AgeGroupDTO.class))).thenReturn(savedEntity);
        when(ageGroupService.save(any(AgeGroup.class))).thenReturn(savedEntity);
        when(ageGroupMapper.toDto(any(AgeGroup.class))).thenReturn(responseDto);

        mockMvc.perform(post("/api/ageGroups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Teenagers"))
                .andExpect(jsonPath("$.description").value("Group for 13-19 years old"))
                .andExpect(jsonPath("$.minAge").value(13))
                .andExpect(jsonPath("$.maxAge").value(19));
    }

    @Test
    @DisplayName("Should return 400 if name is blank")
    void create_BlankName_Returns400() throws Exception {
        AgeGroupDTO invalidDto = new AgeGroupDTO(
                null,
                "",
                "Description",
                10,
                20
        );

        mockMvc.perform(post("/api/ageGroups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 if minAge is null")
    void create_NullMinAge_Returns400() throws Exception {
        AgeGroupDTO invalidDto = new AgeGroupDTO(
                null,
                "Kids",
                "Description",
                null,
                12
        );

        mockMvc.perform(post("/api/ageGroups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 if maxAge is invalid")
    void create_InvalidMaxAge_Returns400() throws Exception {
        AgeGroupDTO invalidDto = new AgeGroupDTO(
                null,
                "Seniors",
                "Description",
                30,
                150
        );

        mockMvc.perform(post("/api/ageGroups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }
}