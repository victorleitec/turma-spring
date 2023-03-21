package br.com.school.admin.controllers;

import br.com.school.admin.exceptions.BusinessRuleException;
import br.com.school.admin.exceptions.ResourceNotFoundException;
import br.com.school.admin.models.Teacher;
import br.com.school.admin.services.TeacherServiceImpl;
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

@WebMvcTest(controllers = TeacherController.class)
class TeacherControllerTest {

    private static final String TEACHER_PATH = "/teachers";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TeacherServiceImpl teacherService;

    @InjectMocks
    private TeacherController teacherController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        standaloneSetup(teacherController)
                .setControllerAdvice(new SchoolControllerAdvice());
    }
    
    /*
    CREATE TEACHER
    1 - Error when teacher name or cpf is empty
    2 - Error when teacher cpf is invalid
    3 - Error when teacher cpf is already registered
    4 - Success when teacher is created
     */

    @Test
    @DisplayName("Should return 400 when trying to create a teacher without name or cpf or specialty")
    void shouldReturn400WhenTryingToCreateATeacherWithoutNameOrCpf() throws Exception {
        // given
        var teacherWithoutName = new Teacher(null, "74539808010", "Math");
        var teacherWithoutCpf = new Teacher("Joseph", null, "Math");
        var teacherWithoutSpecialty = new Teacher("Joseph", "74539808010", null);
        var jsonTeacherWithoutName = objectMapper.writeValueAsString(teacherWithoutName);
        var jsonTeacherWithoutCpf = objectMapper.writeValueAsString(teacherWithoutCpf);
        var jsonTeacherWithoutSpecialty = objectMapper.writeValueAsString(teacherWithoutSpecialty);

        // when
        var requestWithoutName = post(TEACHER_PATH)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonTeacherWithoutName);

        var requestWithoutCpf = post(TEACHER_PATH)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonTeacherWithoutCpf);

        var requestWithoutSpecialty = post(TEACHER_PATH)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonTeacherWithoutSpecialty);

        // then
        mockMvc.perform(requestWithoutName)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Name is required"));

        verify(teacherService, never()).save(any(Teacher.class));

        mockMvc.perform(requestWithoutCpf)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("CPF is required"));

        verify(teacherService, never()).save(any(Teacher.class));

        mockMvc.perform(requestWithoutSpecialty)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Specialty is required"));

        verify(teacherService, never()).save(any(Teacher.class));
    }

    @Test
    @DisplayName("Should return 400 when trying to create a teacher with invalid cpf")
    void shouldReturn400WhenTryingToCreateATeacherWithInvalidCpf() throws Exception {
        // given
        var teacherWithInvalidCpf = new Teacher("Joseph", "12345678910", "Math");
        var jsonTeacherWithInvalidCpf = objectMapper.writeValueAsString(teacherWithInvalidCpf);

        // when
        var requestWithInvalidCpf = post(TEACHER_PATH)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonTeacherWithInvalidCpf);

        // then
        mockMvc.perform(requestWithInvalidCpf)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("CPF is invalid"));

        verify(teacherService, never()).save(any(Teacher.class));
    }

    @Test
    @DisplayName("Should return 400 when trying to create a teacher with cpf that already exists")
    void shouldReturn400WhenTryingToCreateATeacherWithCpfThatAlreadyExists() throws Exception {
        // given
        var teacherWithCpfThatAlreadyExists = new Teacher("Joseph", "74539808010", "Math");
        var jsonTeacherWithCpfThatAlreadyExists = objectMapper.writeValueAsString(teacherWithCpfThatAlreadyExists);
        given(teacherService.save(any())).willThrow(new BusinessRuleException("CPF already exists"));

        // when
        var requestWithCpfThatAlreadyExists = post(TEACHER_PATH)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonTeacherWithCpfThatAlreadyExists);

        // then
        mockMvc.perform(requestWithCpfThatAlreadyExists)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("CPF already exists"));

        verify(teacherService, times(1)).save(any(Teacher.class));
    }

    @Test
    @DisplayName("Should return 201 when trying to create a teacher with valid attributes")
    void shouldReturn201WhenTryingToCreateATeacherWithValidAttributes() throws Exception {
        // given
        var teacherWithValidAttributes = new Teacher("Joseph", "74539808010", "Math");
        var jsonTeacherWithValidAttributes = objectMapper.writeValueAsString(teacherWithValidAttributes);

        given(teacherService.save(any())).willReturn(teacherWithValidAttributes);

        // when
        var requestWithValidAttributes = post(TEACHER_PATH)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonTeacherWithValidAttributes);

        // then
        mockMvc.perform(requestWithValidAttributes)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Joseph"))
                .andExpect(jsonPath("$.cpf").value("74539808010"))
                .andExpect(jsonPath("$.specialty").value("Math"));

        verify(teacherService, times(1)).save(any(Teacher.class));
    }

    /*
    UPDATE TEACHER
    1 - Error when teacher not found
    2 - Error when teacher name or cpf or specialty is empty
    3 - Error when teacher cpf is invalid
    4 - Error when teacher cpf is already registered
    5 - Success when teacher is updated
     */

    @Test
    @DisplayName("Should return 404 when trying to update a teacher that does not exist")
    void shouldReturn404WhenTryingToUpdateATeacherThatDoesNotExist() throws Exception {
        // given
        var nonExistentTeacherId = 5L;
        var jsonTeacherThatDoesNotExist = objectMapper.writeValueAsString(
                new Teacher("Joseph", "74539808010", "Math"));
        given(teacherService.update(anyLong(), any(Teacher.class)))
                .willThrow(new ResourceNotFoundException("Teacher not found"));

        // when
        var pathWithId = TEACHER_PATH + "/" + nonExistentTeacherId;
        var request = put(pathWithId)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonTeacherThatDoesNotExist);

        // then
        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Teacher not found"));

        verify(teacherService, times(1)).update(anyLong(), any(Teacher.class));
    }

    @Test
    @DisplayName("Should return 400 when trying to update a teacher without name or cpf or specialty")
    void shouldReturn400WhenTryingToUpdateATeacherWithoutNameOrCpf() throws Exception {
        // given
        var teacherWithoutName = new Teacher(null, "74539808010", "Math");
        teacherWithoutName.setId(1L);
        var teacherWithoutCpf = new Teacher("Joseph", null, "Math");
        teacherWithoutCpf.setId(2L);
        var teacherWithoutSpecialty = new Teacher("Joseph", "74539808010", null);
        teacherWithoutSpecialty.setId(3L);
        var jsonTeacherWithoutName = objectMapper.writeValueAsString(teacherWithoutName);
        var jsonTeacherWithoutCpf = objectMapper.writeValueAsString(teacherWithoutCpf);
        var jsonTeacherWithoutSpecialty = objectMapper.writeValueAsString(teacherWithoutSpecialty);

        // when
        var pathWithoutName = TEACHER_PATH + "/" + teacherWithoutName.getId();
        var requestWithoutName = put(pathWithoutName)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonTeacherWithoutName);

        var pathWithoutCpf = TEACHER_PATH + "/" + teacherWithoutCpf.getId();
        var requestWithoutCpf = put(pathWithoutCpf)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonTeacherWithoutCpf);

        var pathWithoutSpecialty = TEACHER_PATH + "/" + teacherWithoutSpecialty.getId();
        var requestWithoutSpecialty = put(pathWithoutSpecialty)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonTeacherWithoutSpecialty);

        // then
        mockMvc.perform(requestWithoutName)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Name is required"));

        verify(teacherService, never()).save(any(Teacher.class));

        mockMvc.perform(requestWithoutCpf)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("CPF is required"));

        verify(teacherService, never()).save(any(Teacher.class));

        mockMvc.perform(requestWithoutSpecialty)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Specialty is required"));

        verify(teacherService, never()).save(any(Teacher.class));
    }

    @Test
    @DisplayName("Should return 400 when trying to update a teacher with invalid cpf")
    void shouldReturn400WhenTryingToUpdateATeacherWithInvalidCpf() throws Exception {
        // given
        var teacherWithInvalidCpf = new Teacher("Joseph", "12345678910", "Math");
        teacherWithInvalidCpf.setId(1L);
        var jsonTeacherWithInvalidCpf = objectMapper.writeValueAsString(teacherWithInvalidCpf);

        // when
        var pathWithId = TEACHER_PATH + "/" + teacherWithInvalidCpf.getId();
        var requestWithInvalidCpf = put(pathWithId)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonTeacherWithInvalidCpf);

        // then
        mockMvc.perform(requestWithInvalidCpf)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("CPF is invalid"));

        verify(teacherService, never()).save(any(Teacher.class));
    }

    @Test
    @DisplayName("Should return 400 when trying to update a teacher with cpf that already exists")
    void shouldReturn400WhenTryingToUpdateATeacherWithCpfThatAlreadyExists() throws Exception {
        // given
        var teacherWithCpfThatAlreadyExists = new Teacher("Joseph", "74539808010", "Math");
        teacherWithCpfThatAlreadyExists.setId(1L);
        var jsonTeacherWithCpfThatAlreadyExists = objectMapper.writeValueAsString(teacherWithCpfThatAlreadyExists);
        given(teacherService.update(anyLong(), any(Teacher.class)))
                .willThrow(new BusinessRuleException("CPF already exists"));

        // when
        var pathWithId = TEACHER_PATH + "/" + teacherWithCpfThatAlreadyExists.getId();
        var requestWithCpfThatAlreadyExists = put(pathWithId)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonTeacherWithCpfThatAlreadyExists);

        // then
        mockMvc.perform(requestWithCpfThatAlreadyExists)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("CPF already exists"));
    }

    @Test
    @DisplayName("Should return 200 when trying to update a teacher with valid attributes")
    void shouldReturn200WhenTryingToUpdateATeacherWithValidAttributes() throws Exception {
        // given
        var teacherWithValidAttributes = new Teacher("Joseph", "74539808010", "Math");
        teacherWithValidAttributes.setId(1L);
        var jsonTeacherWithValidAttributes = objectMapper.writeValueAsString(teacherWithValidAttributes);

        given(teacherService.update(anyLong(), any())).willReturn(teacherWithValidAttributes);

        // when
        var pathWithId = TEACHER_PATH + "/" + teacherWithValidAttributes.getId();
        var requestWithValidAttributes = put(pathWithId)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonTeacherWithValidAttributes);

        // then
        mockMvc.perform(requestWithValidAttributes)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Joseph"))
                .andExpect(jsonPath("$.cpf").value("74539808010"))
                .andExpect(jsonPath("$.specialty").value("Math"));

        verify(teacherService, times(1)).update(anyLong(), any(Teacher.class));
    }

    /*
    DELETE TEACHER
    1 - Error when teacher id is not found
    2 - Success when teacher is deleted
     */

    @Test
    @DisplayName("Should return 404 when trying to delete a teacher with id that does not exist")
    void shouldReturn404WhenTryingToDeleteATeacherWithIdThatDoesNotExist() throws Exception {
        // given
        var teacherIdThatDoesNotExist = 1L;
        doThrow(new ResourceNotFoundException("Teacher not found")).when(teacherService).delete(anyLong());
        // when
        var pathWithId = TEACHER_PATH + "/" + teacherIdThatDoesNotExist;
        var requestWithIdThatDoesNotExist = delete(pathWithId)
                .accept(MediaType.APPLICATION_JSON);

        // then
        mockMvc.perform(requestWithIdThatDoesNotExist)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Teacher not found"));

        verify(teacherService, times(1)).delete(anyLong());
    }

    @Test
    @DisplayName("Should return 204 when trying to delete a teacher with id that exists")
    void shouldReturn204WhenTryingToDeleteATeacherWithIdThatExists() throws Exception {
        // given
        var teacherIdThatExists = 1L;

        // when
        var pathWithId = TEACHER_PATH + "/" + teacherIdThatExists;
        var requestWithIdThatExists = delete(pathWithId)
                .accept(MediaType.APPLICATION_JSON);

        // then
        mockMvc.perform(requestWithIdThatExists)
                .andExpect(status().isNoContent());

        verify(teacherService, times(1)).delete(anyLong());
    }

    /*
    GET TEACHER BY ID
    1 - Error when teacher id is not found
    2 - Success when teacher is found
     */

    @Test
    @DisplayName("Should return 404 when trying to find a teacher with id that does not exist")
    void shouldReturn404WhenTryingToFindATeacherWithIdThatDoesNotExist() throws Exception {
        // given
        var teacherIdThatDoesNotExist = 1L;
        given(teacherService.findById(anyLong())).willThrow(new ResourceNotFoundException("Teacher not found"));

        // when
        var pathWithId = TEACHER_PATH + "/" + teacherIdThatDoesNotExist;
        var requestWithIdThatDoesNotExist = get(pathWithId)
                .accept(MediaType.APPLICATION_JSON);

        // then
        mockMvc.perform(requestWithIdThatDoesNotExist)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Teacher not found"));

        verify(teacherService, times(1)).findById(anyLong());
    }

    @Test
    @DisplayName("Should return 200 when trying to find a teacher with id that exists")
    void shouldReturn200WhenTryingToFindATeacherWithIdThatExists() throws Exception {
        // given
        var teacherAlreadyCreated = new Teacher("Joseph", "74539808010", "Math");
        teacherAlreadyCreated.setId(1L);
        given(teacherService.findById(anyLong())).willReturn(teacherAlreadyCreated);

        // when
        var pathWithId = TEACHER_PATH + "/" + teacherAlreadyCreated.getId();
        var requestWithIdThatExists = get(pathWithId)
                .accept(MediaType.APPLICATION_JSON);

        // then
        mockMvc.perform(requestWithIdThatExists)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Joseph"))
                .andExpect(jsonPath("$.cpf").value("74539808010"));
    }

    /*
    GET TEACHERS
    1 - Success when there are teachers
    2 - Success when there are no teachers
     */

    @Test
    @DisplayName("Should return 200 when trying to find all teachers")
    void shouldReturn200WhenTryingToFindAllTeachers() throws Exception {
        // given
        var teacherAlreadyCreated = new Teacher("Joseph", "74539808010", "Math");
        teacherAlreadyCreated.setId(1L);
        var teacherAlreadyCreated2 = new Teacher("Michael", "64173091001", "Science");
        var teachers = List.of(teacherAlreadyCreated, teacherAlreadyCreated2);
        given(teacherService.findAll()).willReturn(teachers);

        // when
        var request = get(TEACHER_PATH)
                .accept(MediaType.APPLICATION_JSON);

        // then
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Joseph"))
                .andExpect(jsonPath("$[0].cpf").value("74539808010"))
                .andExpect(jsonPath("$[0].specialty").value("Math"))
                .andExpect(jsonPath("$[1].name").value("Michael"))
                .andExpect(jsonPath("$[1].cpf").value("64173091001"))
                .andExpect(jsonPath("$[1].specialty").value("Science"));
    }

    @Test
    @DisplayName("Should return 200 when trying to find all teachers but there are no teachers")
    void shouldReturn200WhenTryingToFindAllTeachersButThereAreNoTeachers() throws Exception {
        // given
        var teachers = new ArrayList<Teacher>();
        given(teacherService.findAll()).willReturn(teachers);

        // when
        var request = get(TEACHER_PATH)
                .accept(MediaType.APPLICATION_JSON);

        // then
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }
}