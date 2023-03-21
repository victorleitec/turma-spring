package br.com.school.admin.services;

import br.com.school.admin.exceptions.ResourceNotFoundException;
import br.com.school.admin.models.Director;
import br.com.school.admin.repositories.DirectorCrudRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DirectorServiceImpl {

    private final DirectorCrudRepository repository;
    private final CpfService cpfService;

    public DirectorServiceImpl(DirectorCrudRepository repository, CpfService cpfService) {
        this.repository = repository;
        this.cpfService = cpfService;
    }

    public List<Director> findAll() {
        return repository.findAll();
    }

    public Director findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Director not found"));
    }

    public Director save(Director director) {
        cpfService.checkIfExistsWithCpf(director.getCpf(), null);
        return repository.save(director);
    }

    public Director update(Long id, Director director) {
        var directorToUpdate = findById(id);
        cpfService.checkIfExistsWithCpf(director.getCpf(), directorToUpdate.getCpf());
        directorToUpdate.setName(director.getName());
        directorToUpdate.setCpf(director.getCpf());
        return repository.save(directorToUpdate);
    }

    public void delete(Long id) {
        var directorExists = findById(id);
        repository.delete(directorExists);
    }
}
