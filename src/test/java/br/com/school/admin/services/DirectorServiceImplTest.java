package br.com.school.admin.services;

import br.com.school.admin.exceptions.BusinessRuleException;
import br.com.school.admin.exceptions.ResourceNotFoundException;
import br.com.school.admin.models.Director;
import br.com.school.admin.repositories.DirectorCrudRepository;
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
class DirectorServiceImplTest {

    @Mock
    private DirectorCrudRepository directorCrudRepository;

    @Mock
    private CpfService cpfService;

    @InjectMocks
    private DirectorServiceImpl directorService;

    /*
    CREATE DIRECTOR
    ! 1 - Error when trying to create and cpf already exists
    * 2 - Success when director is created
     */

    @Test
    @DisplayName("Should throw error when trying to create director with existing cpf")
    void testSaveDirectorWithExistingCpfThrowsError() {
        // given
        var cpf = "44007319014";
        var directorWithAlreadyExistingCpf = new Director("Joseph", cpf);

        var expectedException = new BusinessRuleException("CPF already exists");

        given(directorCrudRepository.save(any(Director.class)))
                .willThrow(expectedException);

        // when + then
        var currentException = assertThrows(BusinessRuleException.class,
                () -> directorService.save(directorWithAlreadyExistingCpf));
        assertEquals(expectedException.getMessage(), currentException.getMessage());
        verify(cpfService, times(1)).checkIfExistsWithCpf(cpf, null);
    }

    @Test
    @DisplayName("Should create director")
    void testSaveDirectorSucess() {
        // given
        var director = new Director("Joseph", "44007319014");

        given(directorCrudRepository.save(any(Director.class)))
                .willReturn(director);

        // when
        var createdDirector = directorService.save(director);

        // then
        assertEquals(director.getName(), createdDirector.getName());
        assertEquals(director.getCpf(), createdDirector.getCpf());
        verify(cpfService, times(1)).checkIfExistsWithCpf(director.getCpf(), null);
        verify(directorCrudRepository, times(1)).save(director);
        verifyNoMoreInteractions(directorCrudRepository, cpfService);
    }

    /*
    UPDATE DIRECTOR
    ! 1 - Error when trying to update and director does not exist
    ! 2 - Error when trying to update and cpf already exists
    * 3 - Success when director is updated
     */

    @Test
    @DisplayName("Should throw error when trying to update director that does not exist")
    void testUpdateDirectorThatDoesNotExistThrowsError() {
        // given
        var director = new Director("Joseph", "44007319014");

        var expectedException = new ResourceNotFoundException("Director not found");

        given(directorCrudRepository.findById(1L))
                .willReturn(Optional.empty());

        // when + then
        var currentException = assertThrows(ResourceNotFoundException.class,
                () -> directorService.update(1L, director));
        assertEquals(expectedException.getMessage(), currentException.getMessage());
        verify(directorCrudRepository, times(1)).findById(1L);
        verifyNoMoreInteractions(directorCrudRepository, cpfService);
    }

    @Test
    @DisplayName("Should throw error when trying to update director with existing cpf")
    void testUpdateDirectorWithExistingCpfThrowsError() {
        // given
        var cpf = "44007319014";
        var directorAlreadyExisting = new Director("Harry", cpf);
        var directorWithAlreadyExistingCpf = new Director("Joseph", cpf);

        var expectedException = new BusinessRuleException("CPF already exists");

        given(directorCrudRepository.findById(1L))
                .willReturn(java.util.Optional.of(directorAlreadyExisting));
        given(directorCrudRepository.save(any(Director.class)))
                .willThrow(expectedException);

        // when + then
        var currentException = assertThrows(BusinessRuleException.class,
                () -> directorService.update(1L, directorWithAlreadyExistingCpf));
        assertEquals(expectedException.getMessage(), currentException.getMessage());
        verify(cpfService, times(1)).checkIfExistsWithCpf(cpf, cpf);
    }

    @Test
    @DisplayName("Should update director")
    void testUpdateDirectorSucess() {
        // given
        var savedDirector = new Director("Harry", "44007319014");
        var updatedPendingDirector = new Director("Joseph", "47455321058");
        updatedPendingDirector.setId(1L);

        given(directorCrudRepository.findById(1L))
                .willReturn(Optional.of(savedDirector));
        given(directorCrudRepository.save(any(Director.class)))
                .willReturn(updatedPendingDirector);

        // when
        var updatedDirector = directorService.update(1L, updatedPendingDirector);

        // then
        assertEquals(updatedPendingDirector.getId(), updatedDirector.getId());
        assertEquals(updatedPendingDirector.getName(), updatedDirector.getName());
        assertEquals(updatedPendingDirector.getCpf(), updatedDirector.getCpf());
        verify(directorCrudRepository, times(1)).findById(1L);
        verify(directorCrudRepository, times(1)).save(any(Director.class));
        verify(cpfService, times(1)).checkIfExistsWithCpf(anyString(), anyString());
        verifyNoMoreInteractions(directorCrudRepository, cpfService);
    }

    /*
    DELETE DIRECTOR
    ! 1 - Error when trying to delete and director does not exist
    * 2 - Success when director is deleted
     */

    @Test
    @DisplayName("Should throw error when trying to delete director that does not exist")
    void testDeleteDirectorThatDoesNotExistThrowsError() {
        // given
        var expectedException = new ResourceNotFoundException("Director not found");

        given(directorCrudRepository.findById(1L))
                .willReturn(Optional.empty());

        // when + then
        var currentException = assertThrows(ResourceNotFoundException.class,
                () -> directorService.delete(1L));
        assertEquals(expectedException.getMessage(), currentException.getMessage());
        verify(directorCrudRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should delete director")
    void testDeleteDirectorSucess() {
        // given
        var director = new Director("Harry", "44007319014");
        director.setId(1L);

        given(directorCrudRepository.findById(1L))
                .willReturn(Optional.of(director));
        doNothing().when(directorCrudRepository).delete(director);

        // when + then
        assertDoesNotThrow(() -> directorService.delete(1L));
        verify(directorCrudRepository, times(1)).findById(1L);
        verify(directorCrudRepository, times(1)).delete(director);
    }

    /*
    FIND DIRECTOR BY ID
    ! 1 - Error when trying to find and director does not exist
    * 2 - Success when director is found
     */

    @Test
    @DisplayName("Should throw error when trying to find director that does not exist")
    void testFindDirectorThatDoesNotExistThrowsError() {
        // given
        var expectedException = new ResourceNotFoundException("Director not found");

        given(directorCrudRepository.findById(1L))
                .willReturn(Optional.empty());

        // when + then
        var currentException = assertThrows(ResourceNotFoundException.class,
                () -> directorService.findById(1L));
        assertEquals(expectedException.getMessage(), currentException.getMessage());
        verify(directorCrudRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should find director")
    void testFindDirectorSucess() {
        // given
        var director = new Director("Harry", "44007319014");
        director.setId(1L);

        given(directorCrudRepository.findById(1L))
                .willReturn(Optional.of(director));

        // when
        var foundDirector = directorService.findById(1L);

        // then
        assertEquals(director.getId(), foundDirector.getId());
        assertEquals(director.getName(), foundDirector.getName());
        assertEquals(director.getCpf(), foundDirector.getCpf());
        verify(directorCrudRepository, times(1)).findById(1L);
    }

    /*
    FIND DIRECTORS
    * 1 - Success when directors are found
    * 2 - Sucess when no directors are found
     */

    @Test
    @DisplayName("Should find directors")
    void testFindDirectorsSucess() {
        // given
        var director1 = new Director("Harry", "44007319014");
        director1.setId(1L);
        var director2 = new Director("Joseph", "47455321058");
        director2.setId(2L);

        var directors = List.of(director1, director2);

        given(directorCrudRepository.findAll())
                .willReturn(directors);

        // when
        var foundDirectors = directorService.findAll();

        // then
        assertEquals(directors.size(), foundDirectors.size());
        assertEquals(directors.get(0).getId(), foundDirectors.get(0).getId());
        assertEquals(directors.get(0).getName(), foundDirectors.get(0).getName());
        assertEquals(directors.get(0).getCpf(), foundDirectors.get(0).getCpf());
        assertEquals(directors.get(1).getId(), foundDirectors.get(1).getId());
        assertEquals(directors.get(1).getName(), foundDirectors.get(1).getName());
        assertEquals(directors.get(1).getCpf(), foundDirectors.get(1).getCpf());
        verify(directorCrudRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should find no directors")
    void testFindNoDirectorsSucess() {
        // given
        var directors = new ArrayList<Director>();

        given(directorCrudRepository.findAll())
                .willReturn(directors);

        // when
        var foundDirectors = directorService.findAll();

        // then
        assertEquals(0, foundDirectors.size());
        verify(directorCrudRepository, times(1)).findAll();
    }
}