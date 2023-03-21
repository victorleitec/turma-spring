package br.com.school.admin.controllers;

import br.com.school.admin.exceptions.BusinessRuleException;
import br.com.school.admin.exceptions.ResourceNotFoundException;
import br.com.school.admin.models.Director;
import br.com.school.admin.services.DirectorServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@WebMvcTest(controllers = DirectorController.class)
class DirectorControllerTest {

    private static final String DIRECTOR_PATH = "/directors";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DirectorServiceImpl directorService;

    @InjectMocks
    private DirectorController directorController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        standaloneSetup(directorController)
                .setControllerAdvice(new SchoolControllerAdvice());
    }

    /*
    CREATE DIRECTOR
    1 - Error when director name or cpf is empty
    2 - Error when director cpf is invalid
    3 - Error when director cpf is already registered
    4 - Success when director is created
     */

    @Test
    @DisplayName("Should return 400 when trying to create a director without name or cpf")
    void shouldReturn400WhenTryingToCreateADirectorWithoutNameOrCpf() throws Exception {
        // given
        var directorWithoutName = new Director(null, "74539808010");
        var directorWithoutCpf = new Director("Joseph", null);
        var jsonDirectorWithoutName = objectMapper.writeValueAsString(directorWithoutName);
        var jsonDirectorWithoutCpf = objectMapper.writeValueAsString(directorWithoutCpf);

        // when
        var requestWithoutName = post(DIRECTOR_PATH)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonDirectorWithoutName);

        var requestWithoutCpf = post(DIRECTOR_PATH)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonDirectorWithoutCpf);

        // then
        mockMvc.perform(requestWithoutName)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Name is required"));

        verify(directorService, never()).save(any(Director.class));

        mockMvc.perform(requestWithoutCpf)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("CPF is required"));

        verify(directorService, never()).save(any(Director.class));
    }

    @Test
    @DisplayName("Should return 400 when trying to create a director with invalid cpf")
    void shouldReturn400WhenTryingToCreateADirectorWithInvalidCpf() throws Exception {
        // given
        var directorWithInvalidCpf = new Director("Joseph", "12345678910");
        var jsonDirectorWithInvalidCpf = objectMapper.writeValueAsString(directorWithInvalidCpf);

        // when
        var requestWithInvalidCpf = post(DIRECTOR_PATH)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonDirectorWithInvalidCpf);

        // then
        mockMvc.perform(requestWithInvalidCpf)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("CPF is invalid"));

        verify(directorService, never()).save(any(Director.class));
    }

    @Test
    @DisplayName("Should return 400 when trying to create a director with cpf that already exists")
    void shouldReturn400WhenTryingToCreateADirectorWithCpfThatAlreadyExists() throws Exception {
        // given
        var directorWithCpfThatAlreadyExists = new Director("Joseph", "74539808010");
        var jsonDirectorWithCpfThatAlreadyExists = objectMapper.writeValueAsString(directorWithCpfThatAlreadyExists);
        given(directorService.save(any())).willThrow(new BusinessRuleException("CPF already exists"));

        // when
        var requestWithCpfThatAlreadyExists = post(DIRECTOR_PATH)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonDirectorWithCpfThatAlreadyExists);

        // then
        mockMvc.perform(requestWithCpfThatAlreadyExists)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("CPF already exists"));

        verify(directorService, times(1)).save(any(Director.class));
    }

    @Test
    @DisplayName("Should return 201 when trying to create a director with valid attributes")
    void shouldReturn201WhenTryingToCreateADirectorWithValidAttributes() throws Exception {
        // given
        var directorWithValidAttributes = new Director("Joseph", "74539808010");
        var jsonDirectorWithValidAttributes = objectMapper.writeValueAsString(directorWithValidAttributes);

        given(directorService.save(any())).willReturn(directorWithValidAttributes);

        // when
        var requestWithValidAttributes = post(DIRECTOR_PATH)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonDirectorWithValidAttributes);

        // then
        mockMvc.perform(requestWithValidAttributes)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Joseph"))
                .andExpect(jsonPath("$.cpf").value("74539808010"));

        verify(directorService, times(1)).save(any(Director.class));
    }

    /*
    UPDATE DIRECTOR
    1 - Error when director not found
    2 - Error when director name or cpf is empty
    3 - Error when director cpf is invalid
    4 - Error when director cpf is already registered
    5 - Success when director is updated
     */

    @Test
    @DisplayName("Should return 404 when trying to update a director that does not exist")
    void shouldReturn404WhenTryingToUpdateADirectorThatDoesNotExist() throws Exception {
        // given
        var nonExistentDirectorId = 5L;
        var jsonDirectorThatDoesNotExist = objectMapper.writeValueAsString(
                new Director("Joseph", "74539808010"));
        given(directorService.update(anyLong(), any(Director.class)))
                .willThrow(new ResourceNotFoundException("Director not found"));

        // when
        var pathWithId = DIRECTOR_PATH + "/" + nonExistentDirectorId;
        var request = put(pathWithId)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonDirectorThatDoesNotExist);

        // then
        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Director not found"));

        verify(directorService, times(1)).update(anyLong(), any(Director.class));
    }

    @Test
    @DisplayName("Should return 400 when trying to update a director without name or cpf")
    void shouldReturn400WhenTryingToUpdateADirectorWithoutNameOrCpf() throws Exception {
        // given
        var directorWithoutName = new Director(null, "74539808010");
        directorWithoutName.setId(1L);
        var directorWithoutCpf = new Director("Joseph", null);
        directorWithoutCpf.setId(2L);
        var jsonDirectorWithoutName = objectMapper.writeValueAsString(directorWithoutName);
        var jsonDirectorWithoutCpf = objectMapper.writeValueAsString(directorWithoutCpf);

        // when
        var pathWithoutName = DIRECTOR_PATH + "/" + directorWithoutName.getId();
        var requestWithoutName = put(pathWithoutName)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonDirectorWithoutName);

        var pathWithoutCpf = DIRECTOR_PATH + "/" + directorWithoutCpf.getId();
        var requestWithoutCpf = put(pathWithoutCpf)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonDirectorWithoutCpf);

        // then
        mockMvc.perform(requestWithoutName)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Name is required"));

        verify(directorService, never()).save(any(Director.class));

        mockMvc.perform(requestWithoutCpf)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("CPF is required"));

        verify(directorService, never()).save(any(Director.class));
    }

    @Test
    @DisplayName("Should return 400 when trying to update a director with invalid cpf")
    void shouldReturn400WhenTryingToUpdateADirectorWithInvalidCpf() throws Exception {
        // given
        var directorWithInvalidCpf = new Director("Joseph", "12345678910");
        directorWithInvalidCpf.setId(1L);
        var jsonDirectorWithInvalidCpf = objectMapper.writeValueAsString(directorWithInvalidCpf);

        // when
        var pathWithId = DIRECTOR_PATH + "/" + directorWithInvalidCpf.getId();
        var requestWithInvalidCpf = put(pathWithId)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonDirectorWithInvalidCpf);

        // then
        mockMvc.perform(requestWithInvalidCpf)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("CPF is invalid"));

        verify(directorService, never()).save(any(Director.class));
    }

    @Test
    @DisplayName("Should return 400 when trying to update a director with cpf that already exists")
    void shouldReturn400WhenTryingToUpdateADirectorWithCpfThatAlreadyExists() throws Exception {
        // given
        var directorWithCpfThatAlreadyExists = new Director("Joseph", "74539808010");
        directorWithCpfThatAlreadyExists.setId(1L);
        var jsonDirectorWithCpfThatAlreadyExists = objectMapper.writeValueAsString(directorWithCpfThatAlreadyExists);
        given(directorService.update(anyLong(), any(Director.class)))
                .willThrow(new BusinessRuleException("CPF already exists"));

        // when
        var pathWithId = DIRECTOR_PATH + "/" + directorWithCpfThatAlreadyExists.getId();
        var requestWithCpfThatAlreadyExists = put(pathWithId)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonDirectorWithCpfThatAlreadyExists);

        // then
        mockMvc.perform(requestWithCpfThatAlreadyExists)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("CPF already exists"));
    }

    @Test
    @DisplayName("Should return 200 when trying to update a director with valid attributes")
    void shouldReturn200WhenTryingToUpdateADirectorWithValidAttributes() throws Exception {
        // given
        var directorWithValidAttributes = new Director("Joseph", "74539808010");
        directorWithValidAttributes.setId(1L);
        var jsonDirectorWithValidAttributes = objectMapper.writeValueAsString(directorWithValidAttributes);

        given(directorService.update(anyLong(), any())).willReturn(directorWithValidAttributes);

        // when
        var pathWithId = DIRECTOR_PATH + "/" + directorWithValidAttributes.getId();
        var requestWithValidAttributes = put(pathWithId)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonDirectorWithValidAttributes);

        // then
        mockMvc.perform(requestWithValidAttributes)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Joseph"))
                .andExpect(jsonPath("$.cpf").value("74539808010"));

        verify(directorService, times(1)).update(anyLong(), any(Director.class));
    }

    /*
    DELETE DIRECTOR
    1 - Error when director id is not found
    2 - Success when director is deleted
     */

    @Test
    @DisplayName("Should return 404 when trying to delete a director with id that does not exist")
    void shouldReturn404WhenTryingToDeleteADirectorWithIdThatDoesNotExist() throws Exception {
        // given
        var directorIdThatDoesNotExist = 1L;
        doThrow(new ResourceNotFoundException("Director not found")).when(directorService).delete(anyLong());
        // when
        var pathWithId = DIRECTOR_PATH + "/" + directorIdThatDoesNotExist;
        var requestWithIdThatDoesNotExist = delete(pathWithId)
                .accept(MediaType.APPLICATION_JSON);

        // then
        mockMvc.perform(requestWithIdThatDoesNotExist)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Director not found"));

        verify(directorService, times(1)).delete(anyLong());
    }

    @Test
    @DisplayName("Should return 204 when trying to delete a director with id that exists")
    void shouldReturn204WhenTryingToDeleteADirectorWithIdThatExists() throws Exception {
        // given
        var directorIdThatExists = 1L;

        // when
        var pathWithId = DIRECTOR_PATH + "/" + directorIdThatExists;
        var requestWithIdThatExists = delete(pathWithId)
                .accept(MediaType.APPLICATION_JSON);

        // then
        mockMvc.perform(requestWithIdThatExists)
                .andExpect(status().isNoContent());

        verify(directorService, times(1)).delete(anyLong());
    }

    /*
    GET DIRECTOR BY ID
    1 - Error when director id is not found
    2 - Success when director is found
     */

    @Test
    @DisplayName("Should return 404 when trying to find a director with id that does not exist")
    void shouldReturn404WhenTryingToFindADirectorWithIdThatDoesNotExist() throws Exception {
        // given
        var directorIdThatDoesNotExist = 1L;
        given(directorService.findById(anyLong())).willThrow(new ResourceNotFoundException("Director not found"));

        // when
        var pathWithId = DIRECTOR_PATH + "/" + directorIdThatDoesNotExist;
        var requestWithIdThatDoesNotExist = get(pathWithId)
                .accept(MediaType.APPLICATION_JSON);

        // then
        mockMvc.perform(requestWithIdThatDoesNotExist)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Director not found"));

        verify(directorService, times(1)).findById(anyLong());
    }

    @Test
    @DisplayName("Should return 200 when trying to find a director with id that exists")
    void shouldReturn200WhenTryingToFindADirectorWithIdThatExists() throws Exception {
        // given
        var directorAlreadyCreated = new Director("Joseph", "74539808010");
        directorAlreadyCreated.setId(1L);
        given(directorService.findById(anyLong())).willReturn(directorAlreadyCreated);

        // when
        var pathWithId = DIRECTOR_PATH + "/" + directorAlreadyCreated.getId();
        var requestWithIdThatExists = get(pathWithId)
                .accept(MediaType.APPLICATION_JSON);

        // then
        mockMvc.perform(requestWithIdThatExists)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Joseph"))
                .andExpect(jsonPath("$.cpf").value("74539808010"));
    }

    /*
    GET DIRECTORS
    1 - Success when there are directors
    2 - Success when there are no directors
     */

    @Test
    @DisplayName("Should return 200 when trying to find all directors")
    void shouldReturn200WhenTryingToFindAllDirectors() throws Exception {
        // given
        var directorAlreadyCreated = new Director("Joseph", "74539808010");
        directorAlreadyCreated.setId(1L);
        var directorAlreadyCreated2 = new Director("Michael", "64173091001");
        var directors = List.of(directorAlreadyCreated, directorAlreadyCreated2);
        given(directorService.findAll()).willReturn(directors);

        // when
        var request = get(DIRECTOR_PATH)
                .accept(MediaType.APPLICATION_JSON);

        // then
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Joseph"))
                .andExpect(jsonPath("$[0].cpf").value("74539808010"))
                .andExpect(jsonPath("$[1].name").value("Michael"))
                .andExpect(jsonPath("$[1].cpf").value("64173091001"));
    }

    @Test
    @DisplayName("Should return 200 when trying to find all directors but there are no directors")
    void shouldReturn200WhenTryingToFindAllDirectorsButThereAreNoDirectors() throws Exception {
        // given
        var directors = new ArrayList<Director>();
        given(directorService.findAll()).willReturn(directors);

        // when
        var request = get(DIRECTOR_PATH)
                .accept(MediaType.APPLICATION_JSON);

        // then
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }
}
