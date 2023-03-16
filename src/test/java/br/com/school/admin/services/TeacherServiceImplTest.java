package br.com.school.admin.services;

import br.com.school.admin.exceptions.ResourceNotFoundException;
import br.com.school.admin.models.Teacher;
import br.com.school.admin.repositories.TeacherCrudRepository;
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

class TeacherServiceImplTest {

    @Mock
    private TeacherCrudRepository teacherRepository;
    @Mock
    private CpfService cpfService;

    private TeacherServiceImpl teacherService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        teacherService = new TeacherServiceImpl(teacherRepository, cpfService);
    }


    @Test
    @DisplayName("Should return a list of teachers")
    void shouldReturnAListOfTeachers() {
        // given
        var listOfTeachers = List.of(
                new Teacher("Teacher 1", "12345678900", "Portuguese"),
                new Teacher("Teacher 2", "98765432100", "Math"));
        // when
        Mockito.when(teacherRepository.findAll()).thenReturn(listOfTeachers);
        var allTeachers = teacherService.findAll();
        // then
        assertNotNull(allTeachers);
        assertEquals(listOfTeachers.size(), allTeachers.size());
        assertEquals(listOfTeachers, allTeachers);
        verify(teacherRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should return a teacher by id")
    void shouldReturnTeacherById() {
        // given
        var teacher = new Teacher("Teacher 1", "12345678900", "Portuguese");
        // when
        Mockito.when(teacherRepository.findById(1L)).thenReturn(Optional.of(teacher));
        var teacherFound = teacherService.findById(1L);
        // then
        assertNotNull(teacherFound);
        assertEquals(teacher, teacherFound);
        verify(teacherRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should throw exception when not found teacher by id")
    void shouldThrowExceptionWhenNotFoundTeacherById() {
        // given
        var nonExistentTeacherId = 1L;
        // when
        Mockito.when(teacherRepository.findById(any())).thenReturn(Optional.empty());
        // then
        assertThrows(ResourceNotFoundException.class, () -> teacherService.findById(nonExistentTeacherId));
        verify(teacherRepository, times(1)).findById(nonExistentTeacherId);
    }

    @Test
    @DisplayName("Should save a teacher")
    void shouldSaveTeacher() {
        // given
        var teacher = new Teacher("Teacher 1", "12345678900", "Portuguese");
        // when
        Mockito.when(teacherRepository.save(any())).thenReturn(teacher);
        var savedTeacher = teacherService.save(teacher);
        // then
        assertNotNull(savedTeacher);
        assertEquals(teacher, savedTeacher);
        verify(teacherRepository, times(1)).save(teacher);
    }

    @Test
    @DisplayName("Should throw exception when try to save a teacher with an existing cpf")
    void shouldThrowExceptionWhenTryToSaveTeacherWithExistingCpf() {
        // given
        var teacher = new Teacher("Teacher 1", "12345678900", "Portuguese");
        // when
        Mockito.when(teacherRepository.save(any())).thenReturn(teacher);
        Mockito.doThrow(ResourceNotFoundException.class).when(cpfService).existsByCpfAndDifferentThanCurrentCpf(any(), any());
        // then
        assertThrows(ResourceNotFoundException.class, () -> teacherService.save(teacher));
        verify(teacherRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should update a teacher")
    void shouldUpdateTeacher() {
        // given
        var teacher = new Teacher("Teacher Created", "12345678900", "Portuguese");
        var teacherToUpdate = new Teacher("Teacher Updated", "12345678900", "Portuguese");
        // when
        Mockito.when(teacherRepository.findById(any())).thenReturn(Optional.of(teacher));
        Mockito.when(teacherRepository.save(any())).thenReturn(teacher);
        var updatedTeacher = teacherService.update(1L, teacherToUpdate);
        // then
        assertNotNull(updatedTeacher);
        assertEquals(teacher, updatedTeacher);
        assertEquals(teacherToUpdate.getName(), updatedTeacher.getName());
        verify(teacherRepository, times(1)).findById(1L);
        verify(teacherRepository, times(1)).save(teacher);
    }

    @Test
    @DisplayName("Should throw exception when try to update a teacher with an existing cpf")
    void shouldThrowExceptionWhenTryToUpdateTeacherWithExistingCpf() {
        // given
        var teacher = new Teacher("Teacher Created", "12345678900", "Portuguese");
        var teacherToUpdate = new Teacher("Teacher Updated", "12345678900", "Portuguese");
        // when
        Mockito.when(teacherRepository.findById(any())).thenReturn(Optional.of(teacher));
        Mockito.when(teacherRepository.save(any())).thenReturn(teacher);
        Mockito.doThrow(ResourceNotFoundException.class).when(cpfService).existsByCpfAndDifferentThanCurrentCpf(any(), any());
        // then
        assertThrows(ResourceNotFoundException.class, () -> teacherService.update(1L, teacherToUpdate));
        verify(teacherRepository, times(1)).findById(1L);
        verify(teacherRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when try to update a non existent teacher")
    void shouldThrowExceptionWhenTryToUpdateNonExistentTeacher() {
        // given
        var teacher = new Teacher("Teacher Created", "12345678900", "Portuguese");
        // when
        Mockito.when(teacherRepository.findById(any())).thenReturn(Optional.empty());
        // then
        assertThrows(ResourceNotFoundException.class, () -> teacherService.update(1L, teacher));
        verify(teacherRepository, times(1)).findById(1L);
        verify(teacherRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should delete a teacher")
    void shouldDeleteTeacher() {
        // given
        var teacher = new Teacher("Teacher 1", "12345678900", "Portuguese");
        // when
        Mockito.when(teacherRepository.findById(any())).thenReturn(Optional.of(teacher));
        teacherService.delete(1L);
        // then
        verify(teacherRepository, times(1)).findById(1L);
        verify(teacherRepository, times(1)).delete(teacher);
    }

    @Test
    @DisplayName("Should throw exception when try to delete a non existent teacher")
    void shouldThrowExceptionWhenTryToDeleteNonExistentTeacher() {
        // when
        Mockito.when(teacherRepository.findById(any())).thenReturn(Optional.empty());
        // then
        assertThrows(ResourceNotFoundException.class, () -> teacherService.delete(1L));
        verify(teacherRepository, times(1)).findById(1L);
        verify(teacherRepository, never()).delete(any());
    }
}