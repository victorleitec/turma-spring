package br.com.school.admin.services;

import br.com.school.admin.exceptions.BusinessRuleException;
import br.com.school.admin.repositories.DirectorCrudRepository;
import br.com.school.admin.repositories.StudentCrudRepository;
import br.com.school.admin.repositories.TeacherCrudRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

class CpfServiceTest {

    private StudentCrudRepository studentRepository;

    private TeacherCrudRepository teacherRepository;

    private DirectorCrudRepository directorRepository;

    private CpfService cpfService;

    @BeforeEach
    void setUp() {
        studentRepository = mock(StudentCrudRepository.class);
        teacherRepository = mock(TeacherCrudRepository.class);
        directorRepository = mock(DirectorCrudRepository.class);
        cpfService = new CpfService(studentRepository, teacherRepository, directorRepository);
    }

    /*
    CHECK CPF
    ! 1 - Error when cpf already exists
    * 2 - Sucess when cpf not exists
    * 3 - Sucess when cpf exists but is the same
     */

    @Test
    @DisplayName("Should throw an error when cpf already exists")
    void testCheckCpfThatAlreadyExistsThrowError() {
        // given
        var cpf = "12345678910";
        given(studentRepository.existsByCpf(cpf)).willReturn(true);
        given(teacherRepository.existsByCpf(cpf)).willReturn(false);
        given(directorRepository.existsByCpf(cpf)).willReturn(false);

        // when + then
        assertThrows(BusinessRuleException.class, () -> cpfService.checkIfExistsWithCpf(cpf, null));
        verify(studentRepository, times(1)).existsByCpf(cpf);
        verify(teacherRepository, times(1)).existsByCpf(cpf);
        verify(directorRepository, times(1)).existsByCpf(cpf);
        verifyNoMoreInteractions(studentRepository, teacherRepository, directorRepository);
    }

    @Test
    @DisplayName("Should not throw an error when cpf not exists")
    void testCheckCpfThatNotExistsNotThrowError() {
        // given
        var cpf = "12345678910";
        given(studentRepository.existsByCpf(cpf)).willReturn(false);
        given(teacherRepository.existsByCpf(cpf)).willReturn(false);
        given(directorRepository.existsByCpf(cpf)).willReturn(false);

        // when + then
        assertDoesNotThrow(() -> cpfService.checkIfExistsWithCpf(cpf, null));
        verify(studentRepository, times(1)).existsByCpf(cpf);
        verify(teacherRepository, times(1)).existsByCpf(cpf);
        verify(directorRepository, times(1)).existsByCpf(cpf);
        verifyNoMoreInteractions(studentRepository, teacherRepository, directorRepository);
    }

    @Test
    @DisplayName("Should not throw an error when cpf exists but is the same")
    void testCheckCpfThatExistsButIsTheSameNotThrowError() {
        // given
        var cpf = "12345678910";
        given(studentRepository.existsByCpf(cpf)).willReturn(true);
        given(teacherRepository.existsByCpf(cpf)).willReturn(false);
        given(directorRepository.existsByCpf(cpf)).willReturn(false);

        // when + then
        assertDoesNotThrow(() -> cpfService.checkIfExistsWithCpf(cpf, cpf));
        verify(studentRepository, times(1)).existsByCpf(cpf);
        verify(teacherRepository, times(1)).existsByCpf(cpf);
        verify(directorRepository, times(1)).existsByCpf(cpf);
        verifyNoMoreInteractions(studentRepository, teacherRepository, directorRepository);
    }
}
