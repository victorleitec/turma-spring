package br.com.school.admin.services;

import br.com.school.admin.exceptions.BusinessRuleException;
import br.com.school.admin.exceptions.ResourceNotFoundException;
import br.com.school.admin.models.Teacher;
import br.com.school.admin.repositories.TeacherCrudRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(SpringExtension.class)
class TeacherServiceImplTest {

    @Mock
    private TeacherCrudRepository teacherCrudRepository;

    @Mock
    private CpfService cpfService;

    @InjectMocks
    private TeacherServiceImpl teacherService;

    /*
    CREATE TEACHER
    ! 1 - Error when trying to create and cpf already exists
    * 2 - Success when teacher is created
     */

    @Test
    @DisplayName("Should throw error when trying to create teacher with existing cpf")
    void testSaveTeacherWithExistingCpfThrowsError() {
        // given
        var cpf = "44007319014";
        var teacherWithAlreadyExistingCpf = new Teacher("Joseph", cpf, "Math");

        var expectedException = new BusinessRuleException("CPF already exists");

        given(teacherCrudRepository.save(any(Teacher.class)))
                .willThrow(expectedException);

        // when + then
        var currentException = assertThrows(BusinessRuleException.class,
                () -> teacherService.save(teacherWithAlreadyExistingCpf));
        assertEquals(expectedException.getMessage(), currentException.getMessage());
        verify(cpfService, times(1)).checkIfExistsWithCpf(cpf, null);
    }

    @Test
    @DisplayName("Should create teacher")
    void testSaveTeacherSucess() {
        // given
        var teacher = new Teacher("Joseph", "44007319014", "Math");

        given(teacherCrudRepository.save(any(Teacher.class)))
                .willReturn(teacher);

        // when
        var createdTeacher = teacherService.save(teacher);

        // then
        assertEquals(teacher.getName(), createdTeacher.getName());
        assertEquals(teacher.getCpf(), createdTeacher.getCpf());
        assertEquals(teacher.getSpecialty(), createdTeacher.getSpecialty());
        verify(cpfService, times(1)).checkIfExistsWithCpf(teacher.getCpf(), null);
        verify(teacherCrudRepository, times(1)).save(teacher);
        verifyNoMoreInteractions(teacherCrudRepository, cpfService);
    }

    /*
    UPDATE TEACHER
    ! 1 - Error when trying to update and teacher does not exist
    ! 2 - Error when trying to update and cpf already exists
    * 3 - Success when teacher is updated
     */

    @Test
    @DisplayName("Should throw error when trying to update teacher that does not exist")
    void testUpdateTeacherThatDoesNotExistThrowsError() {
        // given
        var teacher = new Teacher("Joseph", "44007319014", "Math");

        var expectedException = new ResourceNotFoundException("Teacher not found");

        given(teacherCrudRepository.findById(1L))
                .willReturn(Optional.empty());

        // when + then
        var currentException = assertThrows(ResourceNotFoundException.class,
                () -> teacherService.update(1L, teacher));
        assertEquals(expectedException.getMessage(), currentException.getMessage());
        verify(teacherCrudRepository, times(1)).findById(1L);
        verifyNoMoreInteractions(teacherCrudRepository, cpfService);
    }

    @Test
    @DisplayName("Should throw error when trying to update teacher with existing cpf")
    void testUpdateTeacherWithExistingCpfThrowsError() {
        // given
        var cpf = "44007319014";
        var teacherAlreadyExisting = new Teacher("Harry", cpf, "Math");
        var teacherWithAlreadyExistingCpf = new Teacher("Joseph", cpf, "Math");

        var expectedException = new BusinessRuleException("CPF already exists");

        given(teacherCrudRepository.findById(1L))
                .willReturn(java.util.Optional.of(teacherAlreadyExisting));
        given(teacherCrudRepository.save(any(Teacher.class)))
                .willThrow(expectedException);

        // when + then
        var currentException = assertThrows(BusinessRuleException.class,
                () -> teacherService.update(1L, teacherWithAlreadyExistingCpf));
        assertEquals(expectedException.getMessage(), currentException.getMessage());
        verify(cpfService, times(1)).checkIfExistsWithCpf(cpf, cpf);
    }

    @Test
    @DisplayName("Should update teacher")
    void testUpdateTeacherSucess() {
        // given
        var savedTeacher = new Teacher("Harry", "44007319014", "Math");
        var updatedPendingTeacher = new Teacher("Joseph", "47455321058", "Portuguese");
        updatedPendingTeacher.setId(1L);

        given(teacherCrudRepository.findById(1L))
                .willReturn(Optional.of(savedTeacher));
        given(teacherCrudRepository.save(any(Teacher.class)))
                .willReturn(updatedPendingTeacher);

        // when
        var updatedTeacher = teacherService.update(1L, updatedPendingTeacher);

        // then
        assertEquals(updatedPendingTeacher.getId(), updatedTeacher.getId());
        assertEquals(updatedPendingTeacher.getName(), updatedTeacher.getName());
        assertEquals(updatedPendingTeacher.getCpf(), updatedTeacher.getCpf());
        assertEquals(updatedPendingTeacher.getSpecialty(), updatedTeacher.getSpecialty());
        verify(teacherCrudRepository, times(1)).findById(1L);
        verify(teacherCrudRepository, times(1)).save(any(Teacher.class));
        verify(cpfService, times(1)).checkIfExistsWithCpf(anyString(), anyString());
        verifyNoMoreInteractions(teacherCrudRepository, cpfService);
    }

    /*
    DELETE TEACHER
    ! 1 - Error when trying to delete and teacher does not exist
    * 2 - Success when teacher is deleted
     */

    @Test
    @DisplayName("Should throw error when trying to delete teacher that does not exist")
    void testDeleteTeacherThatDoesNotExistThrowsError() {
        // given
        var expectedException = new ResourceNotFoundException("Teacher not found");

        given(teacherCrudRepository.findById(1L))
                .willReturn(Optional.empty());

        // when + then
        var currentException = assertThrows(ResourceNotFoundException.class,
                () -> teacherService.delete(1L));
        assertEquals(expectedException.getMessage(), currentException.getMessage());
        verify(teacherCrudRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should delete teacher")
    void testDeleteTeacherSucess() {
        // given
        var teacher = new Teacher("Harry", "44007319014", "Math");
        teacher.setId(1L);

        given(teacherCrudRepository.findById(1L))
                .willReturn(Optional.of(teacher));
        doNothing().when(teacherCrudRepository).delete(teacher);

        // when + then
        assertDoesNotThrow(() -> teacherService.delete(1L));
        verify(teacherCrudRepository, times(1)).findById(1L);
        verify(teacherCrudRepository, times(1)).delete(teacher);
    }

    /*
    FIND TEACHER BY ID
    ! 1 - Error when trying to find and teacher does not exist
    * 2 - Success when teacher is found
     */

    @Test
    @DisplayName("Should throw error when trying to find teacher that does not exist")
    void testFindTeacherThatDoesNotExistThrowsError() {
        // given
        var expectedException = new ResourceNotFoundException("Teacher not found");

        given(teacherCrudRepository.findById(1L))
                .willReturn(Optional.empty());

        // when + then
        var currentException = assertThrows(ResourceNotFoundException.class,
                () -> teacherService.findById(1L));
        assertEquals(expectedException.getMessage(), currentException.getMessage());
        verify(teacherCrudRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should find teacher")
    void testFindTeacherSucess() {
        // given
        var teacher = new Teacher("Harry", "44007319014", "Math");
        teacher.setId(1L);

        given(teacherCrudRepository.findById(1L))
                .willReturn(Optional.of(teacher));

        // when
        var foundTeacher = teacherService.findById(1L);

        // then
        assertEquals(teacher.getId(), foundTeacher.getId());
        assertEquals(teacher.getName(), foundTeacher.getName());
        assertEquals(teacher.getCpf(), foundTeacher.getCpf());
        assertEquals(teacher.getSpecialty(), foundTeacher.getSpecialty());
        verify(teacherCrudRepository, times(1)).findById(1L);
    }

    /*
    FIND TEACHERS
    * 1 - Success when teachers are found
    * 2 - Sucess when no teachers are found
     */

    @Test
    @DisplayName("Should find teachers")
    void testFindTeachersSucess() {
        // given
        var teacher1 = new Teacher("Harry", "44007319014", "Math");
        teacher1.setId(1L);
        var teacher2 = new Teacher("Joseph", "47455321058", "Portuguese");
        teacher2.setId(2L);

        var teachers = List.of(teacher1, teacher2);

        given(teacherCrudRepository.findAll())
                .willReturn(teachers);

        // when
        var foundTeachers = teacherService.findAll();

        // then
        assertEquals(teachers.size(), foundTeachers.size());
        assertEquals(teachers.get(0).getId(), foundTeachers.get(0).getId());
        assertEquals(teachers.get(0).getName(), foundTeachers.get(0).getName());
        assertEquals(teachers.get(0).getCpf(), foundTeachers.get(0).getCpf());
        assertEquals(teachers.get(0).getSpecialty(), foundTeachers.get(0).getSpecialty());
        assertEquals(teachers.get(1).getId(), foundTeachers.get(1).getId());
        assertEquals(teachers.get(1).getName(), foundTeachers.get(1).getName());
        assertEquals(teachers.get(1).getCpf(), foundTeachers.get(1).getCpf());
        assertEquals(teachers.get(1).getSpecialty(), foundTeachers.get(1).getSpecialty());
        verify(teacherCrudRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should find no teachers")
    void testFindNoTeachersSucess() {
        // given
        var teachers = new ArrayList<Teacher>();

        given(teacherCrudRepository.findAll())
                .willReturn(teachers);

        // when
        var foundTeachers = teacherService.findAll();

        // then
        assertEquals(0, foundTeachers.size());
        verify(teacherCrudRepository, times(1)).findAll();
    }
}