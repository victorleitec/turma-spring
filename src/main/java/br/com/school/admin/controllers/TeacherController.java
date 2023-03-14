package br.com.school.admin.controllers;

import br.com.school.admin.models.Teacher;
import br.com.school.admin.services.DefaultCrudService;
import jakarta.validation.Valid;
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
@RequestMapping("/teachers")
public class TeacherController {

    private final DefaultCrudService<Teacher> service;

    public TeacherController(DefaultCrudService<Teacher> service) {
        this.service = service;
    }

    @GetMapping
    public List<Teacher> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public Teacher findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Teacher save(@RequestBody @Valid Teacher teacher) {
        return service.save(teacher);
    }

    @PutMapping("/{id}")
    public Teacher update(@PathVariable Long id, @RequestBody @Valid Teacher teacher) {
        return service.update(id, teacher);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
