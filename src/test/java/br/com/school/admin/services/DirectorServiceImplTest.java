package br.com.school.admin.services;

import br.com.school.admin.exceptions.ResourceNotFoundException;
import br.com.school.admin.models.Director;
import br.com.school.admin.repositories.DirectorCrudRepository;
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

class DirectorServiceImplTest {

    @Mock
    private DirectorCrudRepository directorRepository;
    @Mock
    private CpfService cpfService;

    private DirectorServiceImpl directorService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        directorService = new DirectorServiceImpl(directorRepository, cpfService);
    }

    @Test
    @DisplayName("Should return a list of directors")
    void shouldReturnAListOfDirectors() {
        // given
        var listOfDirectors = List.of(
                new Director("Director 1", "12345678900"),
                new Director("Director 2", "98765432100")
        );
        // when
        Mockito.when(directorRepository.findAll()).thenReturn(listOfDirectors);
        var allDirectors = directorService.findAll();
        // then
        assertNotNull(allDirectors);
        assertEquals(listOfDirectors.size(), allDirectors.size());
        assertEquals(listOfDirectors, allDirectors);
        verify(directorRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should return a director by id")
    void shouldReturnDirectorById() {
        // given
        var director = new Director("Director 1", "12345678900");
        // when
        Mockito.when(directorRepository.findById(1L)).thenReturn(Optional.of(director));
        var directorFound = directorService.findById(1L);
        // then
        assertNotNull(directorFound);
        assertEquals(director, directorFound);
        verify(directorRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should throw exception when not found director by id")
    void shouldThrowExceptionWhenNotFoundDirectorById() {
        // given
        var nonExistentDirectorId = 1L;
        // when
        Mockito.when(directorRepository.findById(any())).thenReturn(Optional.empty());
        // then
        assertThrows(ResourceNotFoundException.class, () -> directorService.findById(nonExistentDirectorId));
        verify(directorRepository, times(1)).findById(nonExistentDirectorId);
    }

    @Test
    @DisplayName("Should save a director")
    void shouldSaveDirector() {
        // given
        var director = new Director("Director 1", "12345678900");
        // when
        Mockito.when(directorRepository.save(any())).thenReturn(director);
        var savedDirector = directorService.save(director);
        // then
        assertNotNull(savedDirector);
        assertEquals(director, savedDirector);
        verify(directorRepository, times(1)).save(director);
    }

    @Test
    @DisplayName("Should throw exception when try to save a director with an existing cpf")
    void shouldThrowExceptionWhenTryToSaveDirectorWithExistingCpf() {
        // given
        var director = new Director("Director 1", "12345678900");
        // when
        Mockito.when(directorRepository.save(any())).thenReturn(director);
        Mockito.doThrow(ResourceNotFoundException.class).when(cpfService).existsByCpfAndDifferentThanCurrentCpf(any(), any());
        // then
        assertThrows(ResourceNotFoundException.class, () -> directorService.save(director));
        verify(directorRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should update a director")
    void shouldUpdateDirector() {
        // given
        var director = new Director("Director Created", "12345678900");
        var directorToUpdate = new Director("Director Updated", "12345678900");
        // when
        Mockito.when(directorRepository.findById(any())).thenReturn(Optional.of(director));
        Mockito.when(directorRepository.save(any())).thenReturn(director);
        var updatedDirector = directorService.update(1L, directorToUpdate);
        // then
        assertNotNull(updatedDirector);
        assertEquals(director, updatedDirector);
        assertEquals(directorToUpdate.getName(), updatedDirector.getName());
        verify(directorRepository, times(1)).findById(1L);
        verify(directorRepository, times(1)).save(director);
    }

    @Test
    @DisplayName("Should throw exception when try to update a director with an existing cpf")
    void shouldThrowExceptionWhenTryToUpdateDirectorWithExistingCpf() {
        // given
        var director = new Director("Director Created", "12345678900");
        var directorToUpdate = new Director("Director Updated", "12345678900");
        // when
        Mockito.when(directorRepository.findById(any())).thenReturn(Optional.of(director));
        Mockito.when(directorRepository.save(any())).thenReturn(director);
        Mockito.doThrow(ResourceNotFoundException.class).when(cpfService).existsByCpfAndDifferentThanCurrentCpf(any(), any());
        // then
        assertThrows(ResourceNotFoundException.class, () -> directorService.update(1L, directorToUpdate));
        verify(directorRepository, times(1)).findById(1L);
        verify(directorRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when try to update a non existent director")
    void shouldThrowExceptionWhenTryToUpdateNonExistentDirector() {
        // given
        var director = new Director("Director Created", "12345678900");
        // when
        Mockito.when(directorRepository.findById(any())).thenReturn(Optional.empty());
        // then
        assertThrows(ResourceNotFoundException.class, () -> directorService.update(1L, director));
        verify(directorRepository, times(1)).findById(1L);
        verify(directorRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should delete a director")
    void shouldDeleteDirector() {
        // given
        var director = new Director("Director Created", "12345678900");
        // when
        Mockito.when(directorRepository.findById(any())).thenReturn(Optional.of(director));
        directorService.delete(1L);
        // then
        verify(directorRepository, times(1)).findById(1L);
        verify(directorRepository, times(1)).delete(director);
    }

    @Test
    @DisplayName("Should throw exception when try to delete a non existent director")
    void shouldThrowExceptionWhenTryToDeleteNonExistentDirector() {
        // when
        Mockito.when(directorRepository.findById(any())).thenReturn(Optional.empty());
        // then
        assertThrows(ResourceNotFoundException.class, () -> directorService.delete(1L));
        verify(directorRepository, times(1)).findById(1L);
        verify(directorRepository, never()).delete(any());
    }
}