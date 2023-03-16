package br.com.school.admin.services;

import br.com.school.admin.exceptions.ResourceNotFoundException;
import br.com.school.admin.models.Student;
import br.com.school.admin.repositories.StudentCrudRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class StudentServiceImplTest {

    @Mock
    private StudentCrudRepository studentRepository;
    @Mock
    private CpfService cpfService;

    private StudentServiceImpl studentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        studentService = new StudentServiceImpl(studentRepository, cpfService);
    }


    @Test
    @DisplayName("Should return a list of students")
    void shouldReturnAListOfStudents() {
        // given
        var listofStudents = List.of(new Student("Student 1", "12345678900"), new Student("Student 2", "98765432100"));
        // when
        Mockito.when(studentRepository.findAll()).thenReturn(listofStudents);
        var allStudents = studentService.findAll();
        // then
        assertNotNull(allStudents);
        assertEquals(listofStudents.size(), allStudents.size());
        assertEquals(listofStudents, allStudents);
        verify(studentRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should return a student by id")
    void shouldReturnStudentById() {
        // given
        var student = new Student("Student 1", "12345678900");
        // when
        Mockito.when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        var studentFound = studentService.findById(1L);
        // then
        assertNotNull(studentFound);
        assertEquals(student, studentFound);
        verify(studentRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should throw exception when not found student by id")
    void shouldThrowExceptionWhenNotFoundStudentById() {
        // given
        var nonExistentStudentId = 1L;
        // when
        Mockito.when(studentRepository.findById(any())).thenReturn(Optional.empty());
        // then
        assertThrows(ResourceNotFoundException.class, () -> studentService.findById(nonExistentStudentId));
        verify(studentRepository, times(1)).findById(nonExistentStudentId);
    }

    @Test
    @DisplayName("Should save a student")
    void shouldSaveStudent() {
        // given
        var student = new Student("Student 1", "12345678900");
        // when
        Mockito.when(studentRepository.save(any())).thenReturn(student);
        var savedStudent = studentService.save(student);
        // then
        assertNotNull(savedStudent);
        assertEquals(student, savedStudent);
        verify(studentRepository, times(1)).save(student);
    }

    @Test
    @DisplayName("Should throw exception when try to save a student with an existing cpf")
    void shouldThrowExceptionWhenTryToSaveStudentWithExistingCpf() {
        // given
        var student = new Student("Student 1", "12345678900");
        // when
        Mockito.when(studentRepository.save(any())).thenReturn(student);
        Mockito.doThrow(ResourceNotFoundException.class).when(cpfService).existsByCpfAndDifferentThanCurrentCpf(any(), any());
        // then
        assertThrows(ResourceNotFoundException.class, () -> studentService.save(student));
        verify(studentRepository, never()).save(student);
    }

    @Test
    @DisplayName("Should update a student")
    void shouldUpdateStudent() {
        // given
        var student = new Student("Student Created", "12345678900");
        var studentToUpdate = new Student("Student Updated", "12345678900");
        // when
        Mockito.when(studentRepository.findById(any())).thenReturn(Optional.of(student));
        Mockito.when(studentRepository.save(any())).thenReturn(student);
        var updatedStudent = studentService.update(1L, studentToUpdate);
        // then
        assertNotNull(updatedStudent);
        assertEquals(student, updatedStudent);
        assertEquals(studentToUpdate.getName(), updatedStudent.getName());
        verify(studentRepository, times(1)).findById(1L);
        verify(studentRepository, times(1)).save(student);
    }

    @Test
    @DisplayName("Should throw exception when try to update a student with an existing cpf")
    void shouldThrowExceptionWhenTryToUpdateStudentWithExistingCpf() {
        // given
        var student = new Student("Student Created", "12345678900");
        var studentToUpdate = new Student("Student Updated", "12345678900");
        // when
        Mockito.when(studentRepository.findById(any())).thenReturn(Optional.of(student));
        Mockito.when(studentRepository.save(any())).thenReturn(student);
        Mockito.doThrow(ResourceNotFoundException.class).when(cpfService).existsByCpfAndDifferentThanCurrentCpf(any(), any());
        // then
        assertThrows(ResourceNotFoundException.class, () -> studentService.update(1L, studentToUpdate));
        verify(studentRepository, times(1)).findById(1L);
        verify(studentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when try to update a non existent student")
    void shouldThrowExceptionWhenTryToUpdateNonExistentStudent() {
        // given
        var student = new Student("Student Created", "12345678900");
        // when
        Mockito.when(studentRepository.findById(any())).thenReturn(Optional.empty());
        // then
        assertThrows(ResourceNotFoundException.class, () -> studentService.update(1L, student));
        verify(studentRepository, times(1)).findById(1L);
        verify(studentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should delete a student")
    void shouldDeleteStudent() {
        // given
        var student = new Student("Student 1", "12345678900");
        // when
        Mockito.when(studentRepository.findById(any())).thenReturn(Optional.of(student));
        studentService.delete(1L);
        // then
        verify(studentRepository, times(1)).findById(1L);
        verify(studentRepository, times(1)).delete(student);
    }

    @Test
    @DisplayName("Should throw exception when try to delete a non existent student")
    void shouldThrowExceptionWhenTryToDeleteNonExistentStudent() {
        // when
        Mockito.when(studentRepository.findById(any())).thenReturn(Optional.empty());
        // then
        assertThrows(ResourceNotFoundException.class, () -> studentService.delete(1L));
        verify(studentRepository, times(1)).findById(1L);
        verify(studentRepository, never()).delete(any());
    }
}