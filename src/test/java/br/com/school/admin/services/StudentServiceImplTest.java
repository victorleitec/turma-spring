package br.com.school.admin.services;

import br.com.school.admin.exceptions.BusinessRuleException;
import br.com.school.admin.exceptions.ResourceNotFoundException;
import br.com.school.admin.models.Student;
import br.com.school.admin.repositories.StudentCrudRepository;
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
class StudentServiceImplTest {

    @Mock
    private StudentCrudRepository studentCrudRepository;

    @Mock
    private CpfService cpfService;

    @InjectMocks
    private StudentServiceImpl studentService;

    /*
    CREATE STUDENT
    ! 1 - Error when trying to create and cpf already exists
    * 2 - Success when student is created
     */

    @Test
    @DisplayName("Should throw error when trying to create student with existing cpf")
    void testSaveStudentWithExistingCpfThrowsError() {
        // given
        var cpf = "44007319014";
        var studentWithAlreadyExistingCpf = new Student("Joseph", cpf);

        var expectedException = new BusinessRuleException("CPF already exists");

        given(studentCrudRepository.save(any(Student.class)))
                .willThrow(expectedException);

        // when + then
        var currentException = assertThrows(BusinessRuleException.class,
                () -> studentService.save(studentWithAlreadyExistingCpf));
        assertEquals(expectedException.getMessage(), currentException.getMessage());
        verify(cpfService, times(1)).checkIfExistsWithCpf(cpf, null);
    }

    @Test
    @DisplayName("Should create student")
    void testSaveStudentSucess() {
        // given
        var student = new Student("Joseph", "44007319014");

        given(studentCrudRepository.save(any(Student.class)))
                .willReturn(student);

        // when
        var createdStudent = studentService.save(student);

        // then
        assertEquals(student.getName(), createdStudent.getName());
        assertEquals(student.getCpf(), createdStudent.getCpf());
        verify(cpfService, times(1)).checkIfExistsWithCpf(student.getCpf(), null);
        verify(studentCrudRepository, times(1)).save(student);
        verifyNoMoreInteractions(studentCrudRepository, cpfService);
    }

    /*
    UPDATE STUDENT
    ! 1 - Error when trying to update and student does not exist
    ! 2 - Error when trying to update and cpf already exists
    * 3 - Success when student is updated
     */

    @Test
    @DisplayName("Should throw error when trying to update student that does not exist")
    void testUpdateStudentThatDoesNotExistThrowsError() {
        // given
        var student = new Student("Joseph", "44007319014");

        var expectedException = new ResourceNotFoundException("Student not found");

        given(studentCrudRepository.findById(1L))
                .willReturn(Optional.empty());

        // when + then
        var currentException = assertThrows(ResourceNotFoundException.class,
                () -> studentService.update(1L, student));
        assertEquals(expectedException.getMessage(), currentException.getMessage());
        verify(studentCrudRepository, times(1)).findById(1L);
        verifyNoMoreInteractions(studentCrudRepository, cpfService);
    }

    @Test
    @DisplayName("Should throw error when trying to update student with existing cpf")
    void testUpdateStudentWithExistingCpfThrowsError() {
        // given
        var cpf = "44007319014";
        var studentAlreadyExisting = new Student("Harry", cpf);
        var studentWithAlreadyExistingCpf = new Student("Joseph", cpf);

        var expectedException = new BusinessRuleException("CPF already exists");

        given(studentCrudRepository.findById(1L))
                .willReturn(java.util.Optional.of(studentAlreadyExisting));
        given(studentCrudRepository.save(any(Student.class)))
                .willThrow(expectedException);

        // when + then
        var currentException = assertThrows(BusinessRuleException.class,
                () -> studentService.update(1L, studentWithAlreadyExistingCpf));
        assertEquals(expectedException.getMessage(), currentException.getMessage());
        verify(cpfService, times(1)).checkIfExistsWithCpf(cpf, cpf);
    }

    @Test
    @DisplayName("Should update student")
    void testUpdateStudentSucess() {
        // given
        var savedStudent = new Student("Harry", "44007319014");
        var updatedPendingStudent = new Student("Joseph", "47455321058");
        updatedPendingStudent.setId(1L);

        given(studentCrudRepository.findById(1L))
                .willReturn(Optional.of(savedStudent));
        given(studentCrudRepository.save(any(Student.class)))
                .willReturn(updatedPendingStudent);

        // when
        var updatedStudent = studentService.update(1L, updatedPendingStudent);

        // then
        assertEquals(updatedPendingStudent.getId(), updatedStudent.getId());
        assertEquals(updatedPendingStudent.getName(), updatedStudent.getName());
        assertEquals(updatedPendingStudent.getCpf(), updatedStudent.getCpf());
        verify(studentCrudRepository, times(1)).findById(1L);
        verify(studentCrudRepository, times(1)).save(any(Student.class));
        verify(cpfService, times(1)).checkIfExistsWithCpf(anyString(), anyString());
        verifyNoMoreInteractions(studentCrudRepository, cpfService);
    }

    /*
    DELETE STUDENT
    ! 1 - Error when trying to delete and student does not exist
    * 2 - Success when student is deleted
     */

    @Test
    @DisplayName("Should throw error when trying to delete student that does not exist")
    void testDeleteStudentThatDoesNotExistThrowsError() {
        // given
        var expectedException = new ResourceNotFoundException("Student not found");

        given(studentCrudRepository.findById(1L))
                .willReturn(Optional.empty());

        // when + then
        var currentException = assertThrows(ResourceNotFoundException.class,
                () -> studentService.delete(1L));
        assertEquals(expectedException.getMessage(), currentException.getMessage());
        verify(studentCrudRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should delete student")
    void testDeleteStudentSucess() {
        // given
        var student = new Student("Harry", "44007319014");
        student.setId(1L);

        given(studentCrudRepository.findById(1L))
                .willReturn(Optional.of(student));
        doNothing().when(studentCrudRepository).delete(student);

        // when + then
        assertDoesNotThrow(() -> studentService.delete(1L));
        verify(studentCrudRepository, times(1)).findById(1L);
        verify(studentCrudRepository, times(1)).delete(student);
    }

    /*
    FIND STUDENT BY ID
    ! 1 - Error when trying to find and student does not exist
    * 2 - Success when student is found
     */

    @Test
    @DisplayName("Should throw error when trying to find student that does not exist")
    void testFindStudentThatDoesNotExistThrowsError() {
        // given
        var expectedException = new ResourceNotFoundException("Student not found");

        given(studentCrudRepository.findById(1L))
                .willReturn(Optional.empty());

        // when + then
        var currentException = assertThrows(ResourceNotFoundException.class,
                () -> studentService.findById(1L));
        assertEquals(expectedException.getMessage(), currentException.getMessage());
        verify(studentCrudRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should find student")
    void testFindStudentSucess() {
        // given
        var student = new Student("Harry", "44007319014");
        student.setId(1L);

        given(studentCrudRepository.findById(1L))
                .willReturn(Optional.of(student));

        // when
        var foundStudent = studentService.findById(1L);

        // then
        assertEquals(student.getId(), foundStudent.getId());
        assertEquals(student.getName(), foundStudent.getName());
        assertEquals(student.getCpf(), foundStudent.getCpf());
        verify(studentCrudRepository, times(1)).findById(1L);
    }

    /*
    FIND STUDENTS
    * 1 - Success when students are found
    * 2 - Sucess when no students are found
     */

    @Test
    @DisplayName("Should find students")
    void testFindStudentsSucess() {
        // given
        var student1 = new Student("Harry", "44007319014");
        student1.setId(1L);
        var student2 = new Student("Joseph", "47455321058");
        student2.setId(2L);

        var students = List.of(student1, student2);

        given(studentCrudRepository.findAll())
                .willReturn(students);

        // when
        var foundStudents = studentService.findAll();

        // then
        assertEquals(students.size(), foundStudents.size());
        assertEquals(students.get(0).getId(), foundStudents.get(0).getId());
        assertEquals(students.get(0).getName(), foundStudents.get(0).getName());
        assertEquals(students.get(0).getCpf(), foundStudents.get(0).getCpf());
        assertEquals(students.get(1).getId(), foundStudents.get(1).getId());
        assertEquals(students.get(1).getName(), foundStudents.get(1).getName());
        assertEquals(students.get(1).getCpf(), foundStudents.get(1).getCpf());
        verify(studentCrudRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should find no students")
    void testFindNoStudentsSucess() {
        // given
        var students = new ArrayList<Student>();

        given(studentCrudRepository.findAll())
                .willReturn(students);

        // when
        var foundStudents = studentService.findAll();

        // then
        assertEquals(0, foundStudents.size());
        verify(studentCrudRepository, times(1)).findAll();
    }
}