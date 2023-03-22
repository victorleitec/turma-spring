package br.com.school.admin.controllers;

import br.com.school.admin.factories.DirectorFactory;
import br.com.school.admin.models.Director;
import br.com.school.admin.repositories.DirectorCrudRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class DirectorControllerTest {

    private static final String DIRECTOR_PATH = "/directors";

    @Autowired
    MockMvc mockMvc;

    @Autowired
    DirectorCrudRepository directorRepository;

    @Autowired
    ObjectMapper objectMapper;

    private final Long nonExistentId = 999L;
    private final Long existentId = 1L;

    private void generateMultipleData() {
        directorRepository.saveAll(DirectorFactory.createListOfDirectors());
    }

    private Director generateSingleData() {
        return directorRepository.save(DirectorFactory.createDirector());
    }

    /*
    CREATE DIRECTOR
    1 - Error when try to create director with empty name or cpf
    2 - Error when try to create director with invalid cpf
    3 - Error when try to create director with cpf already registered
    4 - Success when try to create a valid director
     */

    @Test
    @DisplayName("Should return error when try to create director with empty name or cpf")
    void shouldReturnErrorWhenTryToCreateDirectorWithEmptyNameOrCpf() throws Exception {
        // given
        var directorEmptyName = DirectorFactory.createDirectorWithEmptyName();
        var directorEmptyCpf = DirectorFactory.createDirectorWithEmptyCpf();

        // when
        var directorEmptyNameRequest = post(DIRECTOR_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(directorEmptyName));

        var directorEmptyCpfRequest = post(DIRECTOR_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(directorEmptyCpf));

        // then
        mockMvc.perform(directorEmptyNameRequest)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value("400"))
                .andExpect(jsonPath("$.message").value("Name is required"));

        mockMvc.perform(directorEmptyCpfRequest)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value("400"))
                .andExpect(jsonPath("$.message").value("CPF is required"));
    }

    @Test
    @DisplayName("Should return error when try to create director with invalid cpf")
    void shouldReturnErrorWhenTryToCreateDirectorWithInvalidCpf() throws Exception {
        // given
        var directorInvalidCpf = DirectorFactory.createDirectorWithInvalidCpf();

        // when
        var directorInvalidCpfRequest = post(DIRECTOR_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(directorInvalidCpf));

        // then
        mockMvc.perform(directorInvalidCpfRequest)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value("400"))
                .andExpect(jsonPath("$.message").value("CPF is invalid"));
    }

    @Test
    @DisplayName("Should return error when try to create director with cpf already registered")
    void shouldReturnErrorWhenTryToCreateDirectorWithCpfAlreadyRegistered() throws Exception {
        // given
        generateMultipleData();
        var directorCpfAlreadyRegistered = DirectorFactory.createDirectorWithCpfAlreadyRegistered();

        // when
        var directorCpfAlreadyRegisteredRequest = post(DIRECTOR_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(directorCpfAlreadyRegistered));

        // then
        mockMvc.perform(directorCpfAlreadyRegisteredRequest)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value("400"))
                .andExpect(jsonPath("$.message").value("CPF already exists"));
    }

    @Test
    @DisplayName("Should return success when try to create a valid director")
    void shouldReturnSuccessWhenTryToCreateAValidDirector() throws Exception {
        // given
        var director = DirectorFactory.createDirector();

        // when
        var directorRequest = post(DIRECTOR_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(director));

        // then
        mockMvc.perform(directorRequest)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(director.getName()))
                .andExpect(jsonPath("$.cpf").value(director.getCpf()));
    }

    /*
    UPDATE DIRECTOR
    1 - Error when try to update non-existent director
    2 - Error when try to update director with empty name or cpf
    3 - Error when try to update director with invalid cpf
    4 - Error when try to update director with cpf already registered
    5 - Ok when try to update a valid director
     */

    @Test
    @DisplayName("Should return error when try to update non-existent director")
    void shouldReturnErrorWhenTryToUpdateNonExistentDirector() throws Exception {
        // given
        var director = DirectorFactory.createDirector();

        // when
        var directorRequest = put(DIRECTOR_PATH + "/{id}", nonExistentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(director));

        // then
        mockMvc.perform(directorRequest)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode").value("404"))
                .andExpect(jsonPath("$.message").value("Director not found"));
    }

    @Test
    @DisplayName("Should return error when try to update director with empty name or cpf")
    void shouldReturnErrorWhenTryToUpdateDirectorWithEmptyNameOrCpf() throws Exception {
        // given
        var directorEmptyName = DirectorFactory.createDirectorWithEmptyName();
        var directorEmptyCpf = DirectorFactory.createDirectorWithEmptyCpf();

        // when
        var directorEmptyNameRequest = put(DIRECTOR_PATH + "/{id}", existentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(directorEmptyName));

        var directorEmptyCpfRequest = put(DIRECTOR_PATH + "/{id}", existentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(directorEmptyCpf));

        // then
        mockMvc.perform(directorEmptyNameRequest)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value("400"))
                .andExpect(jsonPath("$.message").value("Name is required"));

        mockMvc.perform(directorEmptyCpfRequest)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value("400"))
                .andExpect(jsonPath("$.message").value("CPF is required"));
    }

    @Test
    @DisplayName("Should return error when try to update director with invalid cpf")
    void shouldReturnErrorWhenTryToUpdateDirectorWithInvalidCpf() throws Exception {
        // given
        var directorInvalidCpf = DirectorFactory.createDirectorWithInvalidCpf();

        // when
        var directorInvalidCpfRequest = put(DIRECTOR_PATH + "/{id}", existentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(directorInvalidCpf));

        // then
        mockMvc.perform(directorInvalidCpfRequest)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value("400"))
                .andExpect(jsonPath("$.message").value("CPF is invalid"));
    }

    @Test
    @DisplayName("Should return error when try to update director with cpf already registered")
    void shouldReturnErrorWhenTryToUpdateDirectorWithCpfAlreadyRegistered() throws Exception {
        // given
        generateMultipleData();
        var directorCpfAlreadyRegistered = DirectorFactory.createDirectorWithCpfAlreadyRegistered();

        // when
        var directorCpfAlreadyRegisteredRequest = put(DIRECTOR_PATH + "/{id}", existentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(directorCpfAlreadyRegistered));

        // then
        mockMvc.perform(directorCpfAlreadyRegisteredRequest)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value("400"))
                .andExpect(jsonPath("$.message").value("CPF already exists"));
    }

    @Test
    @DisplayName("Should return success when try to update a valid director")
    void shouldReturnSuccessWhenTryToUpdateAValidDirector() throws Exception {
        // given
        var director = generateSingleData();

        // when
        var directorRequest = put(DIRECTOR_PATH + "/{id}", director.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(director));

        // then
        mockMvc.perform(directorRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(director.getName()))
                .andExpect(jsonPath("$.cpf").value(director.getCpf()));
    }

    /*
    DELETE DIRECTOR
    1 - Error when try to delete non-existent director
    2 - No-Content when try to delete a existent director
     */

    @Test
    @DisplayName("Should return error when try to delete non-existent director")
    void shouldReturnErrorWhenTryToDeleteNonExistentDirector() throws Exception {
        // when
        var directorRequest = delete(DIRECTOR_PATH + "/{id}", nonExistentId);

        // then
        mockMvc.perform(directorRequest)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode").value("404"))
                .andExpect(jsonPath("$.message").value("Director not found"));
    }

    @Test
    @DisplayName("Should return no-content when try to delete a existent director")
    void shouldReturnNoContentWhenTryToDeleteAExistentDirector() throws Exception {
        // given
        generateSingleData();

        // when
        var directorRequest = delete(DIRECTOR_PATH + "/{id}", existentId);

        // then
        mockMvc.perform(directorRequest)
                .andExpect(status().isNoContent());
    }


    /*
    GET DIRECTOR BY ID
    1 - Error when try to get non-existent director
    2 - Success when try to get a existent director
     */

    @Test
    @DisplayName("Should return error when try to get non-existent director")
    void shouldReturnErrorWhenTryToGetNonExistentDirector() throws Exception {
        // when
        var directorRequest = get(DIRECTOR_PATH + "/{id}", nonExistentId);

        // then
        mockMvc.perform(directorRequest)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode").value("404"))
                .andExpect(jsonPath("$.message").value("Director not found"));
    }

    @Test
    @DisplayName("Should return success when try to get a existent director")
    void shouldReturnSuccessWhenTryToGetAExistentDirector() throws Exception {
        // given
        var director = generateSingleData();

        // when
        var directorRequest = get(DIRECTOR_PATH + "/{id}", director.getId());

        // then
        mockMvc.perform(directorRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(director.getName()))
                .andExpect(jsonPath("$.cpf").value(director.getCpf()));
    }

    /*
    GET DIRECTORS
    1 - Success when try to get all directors
    2 - Success when try to get all directors and return empty list
     */

    @Test
    @DisplayName("Should return success when try to get all directors")
    void shouldReturnSuccessWhenTryToGetAllDirectors() throws Exception {
        // given
        generateMultipleData();

        // when
        var directorRequest = get(DIRECTOR_PATH);

        // then
        mockMvc.perform(directorRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Joseph"))
                .andExpect(jsonPath("$[0].cpf").value("74539808010"))
                .andExpect(jsonPath("$[1].name").value("John"))
                .andExpect(jsonPath("$[1].cpf").value("40082430039"));
    }

    @Test
    @DisplayName("Should return success when try to get all directors and return empty list")
    void shouldReturnSuccessWhenTryToGetAllDirectorsAndReturnEmptyList() throws Exception {
        // when
        var directorRequest = get(DIRECTOR_PATH);

        // then
        mockMvc.perform(directorRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }
}
