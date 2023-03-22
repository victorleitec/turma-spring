package br.com.school.admin.controllers;

import br.com.school.admin.factories.StudentFactory;
import br.com.school.admin.models.Student;
import br.com.school.admin.repositories.StudentCrudRepository;
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
class StudentControllerTest {

    private static final String STUDENT_PATH = "/students";

    @Autowired
    MockMvc mockMvc;

    @Autowired
    StudentCrudRepository studentRepository;

    @Autowired
    ObjectMapper objectMapper;

    private final Long nonExistentId = 999L;
    private final Long existentId = 1L;

    private void generateMultipleData() {
        studentRepository.saveAll(StudentFactory.createListOfStudents());
    }

    private Student generateSingleData() {
        return studentRepository.save(StudentFactory.createStudent());
    }

    /*
    CREATE STUDENT
    1 - Error when try to create student with empty name or cpf
    2 - Error when try to create student with invalid cpf
    3 - Error when try to create student with cpf already registered
    4 - Success when try to create a valid student
     */

    @Test
    @DisplayName("Should return error when try to create student with empty name or cpf")
    void shouldReturnErrorWhenTryToCreateStudentWithEmptyNameOrCpf() throws Exception {
        // given
        var studentEmptyName = StudentFactory.createStudentWithEmptyName();
        var studentEmptyCpf = StudentFactory.createStudentWithEmptyCpf();

        // when
        var studentEmptyNameRequest = post(STUDENT_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(studentEmptyName));

        var studentEmptyCpfRequest = post(STUDENT_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(studentEmptyCpf));

        // then
        mockMvc.perform(studentEmptyNameRequest)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value("400"))
                .andExpect(jsonPath("$.message").value("Name is required"));

        mockMvc.perform(studentEmptyCpfRequest)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value("400"))
                .andExpect(jsonPath("$.message").value("CPF is required"));
    }

    @Test
    @DisplayName("Should return error when try to create student with invalid cpf")
    void shouldReturnErrorWhenTryToCreateStudentWithInvalidCpf() throws Exception {
        // given
        var studentInvalidCpf = StudentFactory.createStudentWithInvalidCpf();

        // when
        var studentInvalidCpfRequest = post(STUDENT_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(studentInvalidCpf));

        // then
        mockMvc.perform(studentInvalidCpfRequest)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value("400"))
                .andExpect(jsonPath("$.message").value("CPF is invalid"));
    }

    @Test
    @DisplayName("Should return error when try to create student with cpf already registered")
    void shouldReturnErrorWhenTryToCreateStudentWithCpfAlreadyRegistered() throws Exception {
        // given
        generateMultipleData();
        var studentCpfAlreadyRegistered = StudentFactory.createStudentWithCpfAlreadyRegistered();

        // when
        var studentCpfAlreadyRegisteredRequest = post(STUDENT_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(studentCpfAlreadyRegistered));

        // then
        mockMvc.perform(studentCpfAlreadyRegisteredRequest)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value("400"))
                .andExpect(jsonPath("$.message").value("CPF already exists"));
    }

    @Test
    @DisplayName("Should return success when try to create a valid student")
    void shouldReturnSuccessWhenTryToCreateAValidStudent() throws Exception {
        // given
        var student = StudentFactory.createStudent();

        // when
        var studentRequest = post(STUDENT_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(student));

        // then
        mockMvc.perform(studentRequest)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(student.getName()))
                .andExpect(jsonPath("$.cpf").value(student.getCpf()));
    }

    /*
    UPDATE STUDENT
    1 - Error when try to update non-existent student
    2 - Error when try to update student with empty name or cpf
    3 - Error when try to update student with invalid cpf
    4 - Error when try to update student with cpf already registered
    5 - Ok when try to update a valid student
     */

    @Test
    @DisplayName("Should return error when try to update non-existent student")
    void shouldReturnErrorWhenTryToUpdateNonExistentStudent() throws Exception {
        // given
        var student = StudentFactory.createStudent();

        // when
        var studentRequest = put(STUDENT_PATH + "/{id}", nonExistentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(student));

        // then
        mockMvc.perform(studentRequest)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode").value("404"))
                .andExpect(jsonPath("$.message").value("Student not found"));
    }

    @Test
    @DisplayName("Should return error when try to update student with empty name or cpf")
    void shouldReturnErrorWhenTryToUpdateStudentWithEmptyNameOrCpf() throws Exception {
        // given
        var studentEmptyName = StudentFactory.createStudentWithEmptyName();
        var studentEmptyCpf = StudentFactory.createStudentWithEmptyCpf();

        // when
        var studentEmptyNameRequest = put(STUDENT_PATH + "/{id}", existentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(studentEmptyName));

        var studentEmptyCpfRequest = put(STUDENT_PATH + "/{id}", existentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(studentEmptyCpf));

        // then
        mockMvc.perform(studentEmptyNameRequest)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value("400"))
                .andExpect(jsonPath("$.message").value("Name is required"));

        mockMvc.perform(studentEmptyCpfRequest)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value("400"))
                .andExpect(jsonPath("$.message").value("CPF is required"));
    }

    @Test
    @DisplayName("Should return error when try to update student with invalid cpf")
    void shouldReturnErrorWhenTryToUpdateStudentWithInvalidCpf() throws Exception {
        // given
        var studentInvalidCpf = StudentFactory.createStudentWithInvalidCpf();

        // when
        var studentInvalidCpfRequest = put(STUDENT_PATH + "/{id}", existentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(studentInvalidCpf));

        // then
        mockMvc.perform(studentInvalidCpfRequest)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value("400"))
                .andExpect(jsonPath("$.message").value("CPF is invalid"));
    }

    @Test
    @DisplayName("Should return error when try to update student with cpf already registered")
    void shouldReturnErrorWhenTryToUpdateStudentWithCpfAlreadyRegistered() throws Exception {
        // given
        generateMultipleData();
        var studentCpfAlreadyRegistered = StudentFactory.createStudentWithCpfAlreadyRegistered();

        // when
        var studentCpfAlreadyRegisteredRequest = put(STUDENT_PATH + "/{id}", existentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(studentCpfAlreadyRegistered));

        // then
        mockMvc.perform(studentCpfAlreadyRegisteredRequest)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value("400"))
                .andExpect(jsonPath("$.message").value("CPF already exists"));
    }

    @Test
    @DisplayName("Should return success when try to update a valid student")
    void shouldReturnSuccessWhenTryToUpdateAValidStudent() throws Exception {
        // given
        var student = generateSingleData();

        // when
        var studentRequest = put(STUDENT_PATH + "/{id}", student.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(student));

        // then
        mockMvc.perform(studentRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(student.getName()))
                .andExpect(jsonPath("$.cpf").value(student.getCpf()));
    }

    /*
    DELETE STUDENT
    1 - Error when try to delete non-existent student
    2 - No-Content when try to delete a existent student
     */

    @Test
    @DisplayName("Should return error when try to delete non-existent student")
    void shouldReturnErrorWhenTryToDeleteNonExistentStudent() throws Exception {
        // when
        var studentRequest = delete(STUDENT_PATH + "/{id}", nonExistentId);

        // then
        mockMvc.perform(studentRequest)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode").value("404"))
                .andExpect(jsonPath("$.message").value("Student not found"));
    }

    @Test
    @DisplayName("Should return no-content when try to delete a existent student")
    void shouldReturnNoContentWhenTryToDeleteAExistentStudent() throws Exception {
        // given
        generateSingleData();

        // when
        var studentRequest = delete(STUDENT_PATH + "/{id}", existentId);

        // then
        mockMvc.perform(studentRequest)
                .andExpect(status().isNoContent());
    }


    /*
    GET STUDENT BY ID
    1 - Error when try to get non-existent student
    2 - Success when try to get a existent student
     */

    @Test
    @DisplayName("Should return error when try to get non-existent student")
    void shouldReturnErrorWhenTryToGetNonExistentStudent() throws Exception {
        // when
        var studentRequest = get(STUDENT_PATH + "/{id}", nonExistentId);

        // then
        mockMvc.perform(studentRequest)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode").value("404"))
                .andExpect(jsonPath("$.message").value("Student not found"));
    }

    @Test
    @DisplayName("Should return success when try to get a existent student")
    void shouldReturnSuccessWhenTryToGetAExistentStudent() throws Exception {
        // given
        var student = generateSingleData();

        // when
        var studentRequest = get(STUDENT_PATH + "/{id}", student.getId());

        // then
        mockMvc.perform(studentRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(student.getName()))
                .andExpect(jsonPath("$.cpf").value(student.getCpf()));
    }

    /*
    GET STUDENTS
    1 - Success when try to get all students
    2 - Success when try to get all students and return empty list
     */

    @Test
    @DisplayName("Should return success when try to get all students")
    void shouldReturnSuccessWhenTryToGetAllStudents() throws Exception {
        // given
        generateMultipleData();

        // when
        var studentRequest = get(STUDENT_PATH);

        // then
        mockMvc.perform(studentRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Joseph"))
                .andExpect(jsonPath("$[0].cpf").value("74539808010"))
                .andExpect(jsonPath("$[1].name").value("John"))
                .andExpect(jsonPath("$[1].cpf").value("40082430039"));
    }

    @Test
    @DisplayName("Should return success when try to get all students and return empty list")
    void shouldReturnSuccessWhenTryToGetAllStudentsAndReturnEmptyList() throws Exception {
        // when
        var studentRequest = get(STUDENT_PATH);

        // then
        mockMvc.perform(studentRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }
}