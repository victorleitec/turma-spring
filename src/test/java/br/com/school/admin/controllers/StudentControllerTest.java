package br.com.school.admin.controllers;

import br.com.school.admin.exceptions.BusinessRuleException;
import br.com.school.admin.exceptions.ResourceNotFoundException;
import br.com.school.admin.models.Student;
import br.com.school.admin.services.StudentServiceImpl;
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

@WebMvcTest(controllers = StudentController.class)
class StudentControllerTest {

    private static final String STUDENT_PATH = "/students";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StudentServiceImpl studentService;

    @InjectMocks
    private StudentController studentController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        standaloneSetup(studentController)
                .setControllerAdvice(new SchoolControllerAdvice());
    }

    /*
    CREATE STUDENT
    1 - Error when student name or cpf is empty
    2 - Error when student cpf is invalid
    3 - Error when student cpf is already registered
    4 - Success when student is created
     */

    @Test
    @DisplayName("Should return 400 when trying to create a student without name or cpf")
    void shouldReturn400WhenTryingToCreateAStudentWithoutNameOrCpf() throws Exception {
        // given
        var studentWithoutName = new Student(null, "74539808010");
        var studentWithoutCpf = new Student("Joseph", null);
        var jsonStudentWithoutName = objectMapper.writeValueAsString(studentWithoutName);
        var jsonStudentWithoutCpf = objectMapper.writeValueAsString(studentWithoutCpf);

        // when
        var requestWithoutName = post(STUDENT_PATH)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonStudentWithoutName);

        var requestWithoutCpf = post(STUDENT_PATH)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonStudentWithoutCpf);

        // then
        mockMvc.perform(requestWithoutName)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Name is required"));

        verify(studentService, never()).save(any(Student.class));

        mockMvc.perform(requestWithoutCpf)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("CPF is required"));

        verify(studentService, never()).save(any(Student.class));
    }

    @Test
    @DisplayName("Should return 400 when trying to create a student with invalid cpf")
    void shouldReturn400WhenTryingToCreateAStudentWithInvalidCpf() throws Exception {
        // given
        var studentWithInvalidCpf = new Student("Joseph", "12345678910");
        var jsonStudentWithInvalidCpf = objectMapper.writeValueAsString(studentWithInvalidCpf);

        // when
        var requestWithInvalidCpf = post(STUDENT_PATH)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonStudentWithInvalidCpf);

        // then
        mockMvc.perform(requestWithInvalidCpf)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("CPF is invalid"));

        verify(studentService, never()).save(any(Student.class));
    }

    @Test
    @DisplayName("Should return 400 when trying to create a student with cpf that already exists")
    void shouldReturn400WhenTryingToCreateAStudentWithCpfThatAlreadyExists() throws Exception {
        // given
        var studentWithCpfThatAlreadyExists = new Student("Joseph", "74539808010");
        var jsonStudentWithCpfThatAlreadyExists = objectMapper.writeValueAsString(studentWithCpfThatAlreadyExists);
        given(studentService.save(any())).willThrow(new BusinessRuleException("CPF already exists"));

        // when
        var requestWithCpfThatAlreadyExists = post(STUDENT_PATH)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonStudentWithCpfThatAlreadyExists);

        // then
        mockMvc.perform(requestWithCpfThatAlreadyExists)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("CPF already exists"));

        verify(studentService, times(1)).save(any(Student.class));
    }

    @Test
    @DisplayName("Should return 201 when trying to create a student with valid attributes")
    void shouldReturn201WhenTryingToCreateAStudentWithValidAttributes() throws Exception {
        // given
        var studentWithValidAttributes = new Student("Joseph", "74539808010");
        var jsonStudentWithValidAttributes = objectMapper.writeValueAsString(studentWithValidAttributes);

        given(studentService.save(any())).willReturn(studentWithValidAttributes);

        // when
        var requestWithValidAttributes = post(STUDENT_PATH)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonStudentWithValidAttributes);

        // then
        mockMvc.perform(requestWithValidAttributes)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Joseph"))
                .andExpect(jsonPath("$.cpf").value("74539808010"));

        verify(studentService, times(1)).save(any(Student.class));
    }

    /*
    UPDATE STUDENT
    1 - Error when student not found
    2 - Error when student name or cpf is empty
    3 - Error when student cpf is invalid
    4 - Error when student cpf is already registered
    5 - Success when student is updated
     */

    @Test
    @DisplayName("Should return 404 when trying to update a student that does not exist")
    void shouldReturn404WhenTryingToUpdateAStudentThatDoesNotExist() throws Exception {
        // given
        var nonExistentStudentId = 5L;
        var jsonStudentThatDoesNotExist = objectMapper.writeValueAsString(
                new Student("Joseph", "74539808010"));
        given(studentService.update(anyLong(), any(Student.class)))
                .willThrow(new ResourceNotFoundException("Student not found"));

        // when
        var pathWithId = STUDENT_PATH + "/" + nonExistentStudentId;
        var request = put(pathWithId)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonStudentThatDoesNotExist);

        // then
        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Student not found"));

        verify(studentService, times(1)).update(anyLong(), any(Student.class));
    }

    @Test
    @DisplayName("Should return 400 when trying to update a student without name or cpf")
    void shouldReturn400WhenTryingToUpdateAStudentWithoutNameOrCpf() throws Exception {
        // given
        var studentWithoutName = new Student(null, "74539808010");
        studentWithoutName.setId(1L);
        var studentWithoutCpf = new Student("Joseph", null);
        studentWithoutCpf.setId(2L);
        var jsonStudentWithoutName = objectMapper.writeValueAsString(studentWithoutName);
        var jsonStudentWithoutCpf = objectMapper.writeValueAsString(studentWithoutCpf);

        // when
        var pathWithoutName = STUDENT_PATH + "/" + studentWithoutName.getId();
        var requestWithoutName = put(pathWithoutName)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonStudentWithoutName);

        var pathWithoutCpf = STUDENT_PATH + "/" + studentWithoutCpf.getId();
        var requestWithoutCpf = put(pathWithoutCpf)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonStudentWithoutCpf);

        // then
        mockMvc.perform(requestWithoutName)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Name is required"));

        verify(studentService, never()).save(any(Student.class));

        mockMvc.perform(requestWithoutCpf)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("CPF is required"));

        verify(studentService, never()).save(any(Student.class));
    }

    @Test
    @DisplayName("Should return 400 when trying to update a student with invalid cpf")
    void shouldReturn400WhenTryingToUpdateAStudentWithInvalidCpf() throws Exception {
        // given
        var studentWithInvalidCpf = new Student("Joseph", "12345678910");
        studentWithInvalidCpf.setId(1L);
        var jsonStudentWithInvalidCpf = objectMapper.writeValueAsString(studentWithInvalidCpf);

        // when
        var pathWithId = STUDENT_PATH + "/" + studentWithInvalidCpf.getId();
        var requestWithInvalidCpf = put(pathWithId)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonStudentWithInvalidCpf);

        // then
        mockMvc.perform(requestWithInvalidCpf)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("CPF is invalid"));

        verify(studentService, never()).save(any(Student.class));
    }

    @Test
    @DisplayName("Should return 400 when trying to update a student with cpf that already exists")
    void shouldReturn400WhenTryingToUpdateAStudentWithCpfThatAlreadyExists() throws Exception {
        // given
        var studentWithCpfThatAlreadyExists = new Student("Joseph", "74539808010");
        studentWithCpfThatAlreadyExists.setId(1L);
        var jsonStudentWithCpfThatAlreadyExists = objectMapper.writeValueAsString(studentWithCpfThatAlreadyExists);
        given(studentService.update(anyLong(), any(Student.class)))
                .willThrow(new BusinessRuleException("CPF already exists"));

        // when
        var pathWithId = STUDENT_PATH + "/" + studentWithCpfThatAlreadyExists.getId();
        var requestWithCpfThatAlreadyExists = put(pathWithId)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonStudentWithCpfThatAlreadyExists);

        // then
        mockMvc.perform(requestWithCpfThatAlreadyExists)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("CPF already exists"));
    }

    @Test
    @DisplayName("Should return 200 when trying to update a student with valid attributes")
    void shouldReturn200WhenTryingToUpdateAStudentWithValidAttributes() throws Exception {
        // given
        var studentWithValidAttributes = new Student("Joseph", "74539808010");
        studentWithValidAttributes.setId(1L);
        var jsonStudentWithValidAttributes = objectMapper.writeValueAsString(studentWithValidAttributes);

        given(studentService.update(anyLong(), any())).willReturn(studentWithValidAttributes);

        // when
        var pathWithId = STUDENT_PATH + "/" + studentWithValidAttributes.getId();
        var requestWithValidAttributes = put(pathWithId)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonStudentWithValidAttributes);

        // then
        mockMvc.perform(requestWithValidAttributes)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Joseph"))
                .andExpect(jsonPath("$.cpf").value("74539808010"));

        verify(studentService, times(1)).update(anyLong(), any(Student.class));
    }

    /*
    DELETE STUDENT
    1 - Error when student id is not found
    2 - Success when student is deleted
     */

    @Test
    @DisplayName("Should return 404 when trying to delete a student with id that does not exist")
    void shouldReturn404WhenTryingToDeleteAStudentWithIdThatDoesNotExist() throws Exception {
        // given
        var studentIdThatDoesNotExist = 1L;
        doThrow(new ResourceNotFoundException("Student not found")).when(studentService).delete(anyLong());
        // when
        var pathWithId = STUDENT_PATH + "/" + studentIdThatDoesNotExist;
        var requestWithIdThatDoesNotExist = delete(pathWithId)
                .accept(MediaType.APPLICATION_JSON);

        // then
        mockMvc.perform(requestWithIdThatDoesNotExist)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Student not found"));

        verify(studentService, times(1)).delete(anyLong());
    }

    @Test
    @DisplayName("Should return 204 when trying to delete a student with id that exists")
    void shouldReturn204WhenTryingToDeleteAStudentWithIdThatExists() throws Exception {
        // given
        var studentIdThatExists = 1L;

        // when
        var pathWithId = STUDENT_PATH + "/" + studentIdThatExists;
        var requestWithIdThatExists = delete(pathWithId)
                .accept(MediaType.APPLICATION_JSON);

        // then
        mockMvc.perform(requestWithIdThatExists)
                .andExpect(status().isNoContent());

        verify(studentService, times(1)).delete(anyLong());
    }

    /*
    GET STUDENT BY ID
    1 - Error when student id is not found
    2 - Success when student is found
     */

    @Test
    @DisplayName("Should return 404 when trying to find a student with id that does not exist")
    void shouldReturn404WhenTryingToFindAStudentWithIdThatDoesNotExist() throws Exception {
        // given
        var studentIdThatDoesNotExist = 1L;
        given(studentService.findById(anyLong())).willThrow(new ResourceNotFoundException("Student not found"));

        // when
        var pathWithId = STUDENT_PATH + "/" + studentIdThatDoesNotExist;
        var requestWithIdThatDoesNotExist = get(pathWithId)
                .accept(MediaType.APPLICATION_JSON);

        // then
        mockMvc.perform(requestWithIdThatDoesNotExist)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Student not found"));

        verify(studentService, times(1)).findById(anyLong());
    }

    @Test
    @DisplayName("Should return 200 when trying to find a student with id that exists")
    void shouldReturn200WhenTryingToFindAStudentWithIdThatExists() throws Exception {
        // given
        var studentAlreadyCreated = new Student("Joseph", "74539808010");
        studentAlreadyCreated.setId(1L);
        given(studentService.findById(anyLong())).willReturn(studentAlreadyCreated);

        // when
        var pathWithId = STUDENT_PATH + "/" + studentAlreadyCreated.getId();
        var requestWithIdThatExists = get(pathWithId)
                .accept(MediaType.APPLICATION_JSON);

        // then
        mockMvc.perform(requestWithIdThatExists)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Joseph"))
                .andExpect(jsonPath("$.cpf").value("74539808010"));
    }

    /*
    GET STUDENTS
    1 - Success when there are students
    2 - Success when there are no students
     */

    @Test
    @DisplayName("Should return 200 when trying to find all students")
    void shouldReturn200WhenTryingToFindAllStudents() throws Exception {
        // given
        var studentAlreadyCreated = new Student("Joseph", "74539808010");
        studentAlreadyCreated.setId(1L);
        var studentAlreadyCreated2 = new Student("Michael", "64173091001");
        var students = List.of(studentAlreadyCreated, studentAlreadyCreated2);
        given(studentService.findAll()).willReturn(students);

        // when
        var request = get(STUDENT_PATH)
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
    @DisplayName("Should return 200 when trying to find all students but there are no students")
    void shouldReturn200WhenTryingToFindAllStudentsButThereAreNoStudents() throws Exception {
        // given
        var students = new ArrayList<Student>();
        given(studentService.findAll()).willReturn(students);

        // when
        var request = get(STUDENT_PATH)
                .accept(MediaType.APPLICATION_JSON);

        // then
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }
}
