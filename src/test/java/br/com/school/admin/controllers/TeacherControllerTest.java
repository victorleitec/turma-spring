package br.com.school.admin.controllers;

import br.com.school.admin.factories.TeacherFactory;
import br.com.school.admin.models.Teacher;
import br.com.school.admin.repositories.TeacherCrudRepository;
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
class TeacherControllerTest {

    private static final String TEACHER_PATH = "/teachers";

    @Autowired
    MockMvc mockMvc;

    @Autowired
    TeacherCrudRepository teacherRepository;

    @Autowired
    ObjectMapper objectMapper;

    private final Long nonExistentId = 999L;
    private final Long existentId = 1L;

    private void generateMultipleData() {
        teacherRepository.saveAll(TeacherFactory.createListOfTeachers());
    }

    private Teacher generateSingleData() {
        return teacherRepository.save(TeacherFactory.createTeacher());
    }

    /*
    CREATE TEACHER
    1 - Error when try to create teacher with empty name or cpf or specialty
    2 - Error when try to create teacher with invalid cpf
    3 - Error when try to create teacher with cpf already registered
    4 - Success when try to create a valid teacher
     */

    @Test
    @DisplayName("Should return error when try to create teacher with empty name or cpf")
    void shouldReturnErrorWhenTryToCreateTeacherWithEmptyNameOrCpf() throws Exception {
        // given
        var teacherEmptyName = TeacherFactory.createTeacherWithEmptyName();
        var teacherEmptyCpf = TeacherFactory.createTeacherWithEmptyCpf();
        var teacherEmptySpecialty = TeacherFactory.createTeacherWithEmptySpecialty();

        // when
        var teacherEmptyNameRequest = post(TEACHER_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(teacherEmptyName));

        var teacherEmptyCpfRequest = post(TEACHER_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(teacherEmptyCpf));

        var teacherEmptySpecialtyRequest = post(TEACHER_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(teacherEmptySpecialty));

        // then
        mockMvc.perform(teacherEmptyNameRequest)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value("400"))
                .andExpect(jsonPath("$.message").value("Name is required"));

        mockMvc.perform(teacherEmptyCpfRequest)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value("400"))
                .andExpect(jsonPath("$.message").value("CPF is required"));

        mockMvc.perform(teacherEmptySpecialtyRequest)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value("400"))
                .andExpect(jsonPath("$.message").value("Specialty is required"));
    }

    @Test
    @DisplayName("Should return error when try to create teacher with invalid cpf")
    void shouldReturnErrorWhenTryToCreateTeacherWithInvalidCpf() throws Exception {
        // given
        var teacherInvalidCpf = TeacherFactory.createTeacherWithInvalidCpf();

        // when
        var teacherInvalidCpfRequest = post(TEACHER_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(teacherInvalidCpf));

        // then
        mockMvc.perform(teacherInvalidCpfRequest)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value("400"))
                .andExpect(jsonPath("$.message").value("CPF is invalid"));
    }

    @Test
    @DisplayName("Should return error when try to create teacher with cpf already registered")
    void shouldReturnErrorWhenTryToCreateTeacherWithCpfAlreadyRegistered() throws Exception {
        // given
        generateMultipleData();
        var teacherCpfAlreadyRegistered = TeacherFactory.createTeacherWithCpfAlreadyRegistered();

        // when
        var teacherCpfAlreadyRegisteredRequest = post(TEACHER_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(teacherCpfAlreadyRegistered));

        // then
        mockMvc.perform(teacherCpfAlreadyRegisteredRequest)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value("400"))
                .andExpect(jsonPath("$.message").value("CPF already exists"));
    }

    @Test
    @DisplayName("Should return success when try to create a valid teacher")
    void shouldReturnSuccessWhenTryToCreateAValidTeacher() throws Exception {
        // given
        var teacher = TeacherFactory.createTeacher();

        // when
        var teacherRequest = post(TEACHER_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(teacher));

        // then
        mockMvc.perform(teacherRequest)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(teacher.getName()))
                .andExpect(jsonPath("$.cpf").value(teacher.getCpf()));
    }

    /*
    UPDATE TEACHER
    1 - Error when try to update non-existent teacher
    2 - Error when try to update teacher with empty name or cpf or specialty
    3 - Error when try to update teacher with invalid cpf
    4 - Error when try to update teacher with cpf already registered
    5 - Ok when try to update a valid teacher
     */

    @Test
    @DisplayName("Should return error when try to update non-existent teacher")
    void shouldReturnErrorWhenTryToUpdateNonExistentTeacher() throws Exception {
        // given
        var teacher = TeacherFactory.createTeacher();

        // when
        var teacherRequest = put(TEACHER_PATH + "/{id}", nonExistentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(teacher));

        // then
        mockMvc.perform(teacherRequest)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode").value("404"))
                .andExpect(jsonPath("$.message").value("Teacher not found"));
    }

    @Test
    @DisplayName("Should return error when try to update teacher with empty name or cpf")
    void shouldReturnErrorWhenTryToUpdateTeacherWithEmptyNameOrCpf() throws Exception {
        // given
        var teacherEmptyName = TeacherFactory.createTeacherWithEmptyName();
        var teacherEmptyCpf = TeacherFactory.createTeacherWithEmptyCpf();
        var teacherEmptySpecialty = TeacherFactory.createTeacherWithEmptySpecialty();

        // when
        var teacherEmptyNameRequest = put(TEACHER_PATH + "/{id}", existentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(teacherEmptyName));

        var teacherEmptyCpfRequest = put(TEACHER_PATH + "/{id}", existentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(teacherEmptyCpf));

        var teacherEmptySpecialtyRequest = put(TEACHER_PATH + "/{id}", existentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(teacherEmptySpecialty));

        // then
        mockMvc.perform(teacherEmptyNameRequest)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value("400"))
                .andExpect(jsonPath("$.message").value("Name is required"));

        mockMvc.perform(teacherEmptyCpfRequest)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value("400"))
                .andExpect(jsonPath("$.message").value("CPF is required"));

        mockMvc.perform(teacherEmptySpecialtyRequest)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value("400"))
                .andExpect(jsonPath("$.message").value("Specialty is required"));
    }

    @Test
    @DisplayName("Should return error when try to update teacher with invalid cpf")
    void shouldReturnErrorWhenTryToUpdateTeacherWithInvalidCpf() throws Exception {
        // given
        var teacherInvalidCpf = TeacherFactory.createTeacherWithInvalidCpf();

        // when
        var teacherInvalidCpfRequest = put(TEACHER_PATH + "/{id}", existentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(teacherInvalidCpf));

        // then
        mockMvc.perform(teacherInvalidCpfRequest)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value("400"))
                .andExpect(jsonPath("$.message").value("CPF is invalid"));
    }

    @Test
    @DisplayName("Should return error when try to update teacher with cpf already registered")
    void shouldReturnErrorWhenTryToUpdateTeacherWithCpfAlreadyRegistered() throws Exception {
        // given
        generateMultipleData();
        var teacherCpfAlreadyRegistered = TeacherFactory.createTeacherWithCpfAlreadyRegistered();

        // when
        var teacherCpfAlreadyRegisteredRequest = put(TEACHER_PATH + "/{id}", existentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(teacherCpfAlreadyRegistered));

        // then
        mockMvc.perform(teacherCpfAlreadyRegisteredRequest)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value("400"))
                .andExpect(jsonPath("$.message").value("CPF already exists"));
    }

    @Test
    @DisplayName("Should return success when try to update a valid teacher")
    void shouldReturnSuccessWhenTryToUpdateAValidTeacher() throws Exception {
        // given
        var teacher = generateSingleData();

        // when
        var teacherRequest = put(TEACHER_PATH + "/{id}", teacher.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(teacher));

        // then
        mockMvc.perform(teacherRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(teacher.getName()))
                .andExpect(jsonPath("$.cpf").value(teacher.getCpf()));
    }

    /*
    DELETE TEACHER
    1 - Error when try to delete non-existent teacher
    2 - No-Content when try to delete existent teacher
     */

    @Test
    @DisplayName("Should return error when try to delete non-existent teacher")
    void shouldReturnErrorWhenTryToDeleteNonExistentTeacher() throws Exception {
        // when
        var teacherRequest = delete(TEACHER_PATH + "/{id}", nonExistentId);

        // then
        mockMvc.perform(teacherRequest)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode").value("404"))
                .andExpect(jsonPath("$.message").value("Teacher not found"));
    }

    @Test
    @DisplayName("Should return no-content when try to delete a existent teacher")
    void shouldReturnNoContentWhenTryToDeleteAExistentTeacher() throws Exception {
        // given
        generateSingleData();

        // when
        var teacherRequest = delete(TEACHER_PATH + "/{id}", existentId);

        // then
        mockMvc.perform(teacherRequest)
                .andExpect(status().isNoContent());
    }


    /*
    GET TEACHER BY ID
    1 - Error when try to get non-existent teacher
    2 - Success when try to get existent teacher
     */

    @Test
    @DisplayName("Should return error when try to get non-existent teacher")
    void shouldReturnErrorWhenTryToGetNonExistentTeacher() throws Exception {
        // when
        var teacherRequest = get(TEACHER_PATH + "/{id}", nonExistentId);

        // then
        mockMvc.perform(teacherRequest)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode").value("404"))
                .andExpect(jsonPath("$.message").value("Teacher not found"));
    }

    @Test
    @DisplayName("Should return success when try to get a existent teacher")
    void shouldReturnSuccessWhenTryToGetAExistentTeacher() throws Exception {
        // given
        var teacher = generateSingleData();

        // when
        var teacherRequest = get(TEACHER_PATH + "/{id}", teacher.getId());

        // then
        mockMvc.perform(teacherRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(teacher.getName()))
                .andExpect(jsonPath("$.cpf").value(teacher.getCpf()));
    }

    /*
    GET TEACHERS
    1 - Success when try to get all teachers
    2 - Success when try to get all teachers and return empty list
     */

    @Test
    @DisplayName("Should return success when try to get all teachers")
    void shouldReturnSuccessWhenTryToGetAllTeachers() throws Exception {
        // given
        generateMultipleData();

        // when
        var teacherRequest = get(TEACHER_PATH);

        // then
        mockMvc.perform(teacherRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Joseph"))
                .andExpect(jsonPath("$[0].cpf").value("74539808010"))
                .andExpect(jsonPath("$[1].name").value("John"))
                .andExpect(jsonPath("$[1].cpf").value("40082430039"));
    }

    @Test
    @DisplayName("Should return success when try to get all teachers and return empty list")
    void shouldReturnSuccessWhenTryToGetAllTeachersAndReturnEmptyList() throws Exception {
        // when
        var teacherRequest = get(TEACHER_PATH);

        // then
        mockMvc.perform(teacherRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }
}