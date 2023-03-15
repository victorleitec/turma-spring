package br.com.school.admin.controllers;

import br.com.school.admin.models.Director;
import br.com.school.admin.services.DirectorServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/directors")
public class DirectorController {

    private final DirectorServiceImpl directorServiceImpl;

    public DirectorController(DirectorServiceImpl directorServiceImpl) {
        this.directorServiceImpl = directorServiceImpl;
    }

    @GetMapping
    public List<Director> findAll() {
        return directorServiceImpl.findAll();
    }

    @GetMapping("/{id}")
    public Director findById(@PathVariable Long id) {
        return directorServiceImpl.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Director save(@RequestBody Director director) {
        return directorServiceImpl.save(director);
    }

    @PutMapping("/{id}")
    public Director update(@PathVariable Long id, @RequestBody Director director) {
        return directorServiceImpl.update(id, director);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        directorServiceImpl.delete(id);
    }
}
