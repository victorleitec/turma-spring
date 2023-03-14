package br.com.school.admin.services;

import br.com.school.admin.exceptions.ResourceNotFoundException;
import br.com.school.admin.models.Teacher;
import br.com.school.admin.repositories.TeacherCrudRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TeacherServiceImpl implements DefaultCrudService<Teacher> {

    private final TeacherCrudRepository repository;
    private final CpfService cpfService;

    public TeacherServiceImpl(TeacherCrudRepository repository, CpfService cpfService) {
        this.repository = repository;
        this.cpfService = cpfService;
    }

    @Override
    public List<Teacher> findAll() {
        return repository.findAll();
    }

    @Override
    public Teacher findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found"));
    }

    @Override
    public Teacher save(Teacher teacher) {
        cpfService.existsByCpfAndDifferentThanCurrentCpf(teacher.getCpf(), null);
        return repository.save(teacher);
    }

    @Override
    public Teacher update(Long id, Teacher teacher) {
        var teacherToUpdate = findById(id);
        cpfService.existsByCpfAndDifferentThanCurrentCpf(teacher.getCpf(), teacherToUpdate.getCpf());
        teacherToUpdate.setName(teacher.getName());
        teacherToUpdate.setCpf(teacher.getCpf());
        teacherToUpdate.setSpecialty(teacher.getSpecialty());
        return repository.save(teacherToUpdate);
    }

    @Override
    public void delete(Long id) {
        var teacherExists = findById(id);
        repository.delete(teacherExists);
    }
}
