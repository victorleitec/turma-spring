package br.com.school.admin.services;

import br.com.school.admin.exceptions.ResourceNotFoundException;
import br.com.school.admin.models.Student;
import br.com.school.admin.repositories.StudentCrudRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StudentServiceImpl implements DefaultCrudService<Student> {

    private final StudentCrudRepository repository;
    private final CpfService cpfService;

    public StudentServiceImpl(StudentCrudRepository repository, CpfService cpfService) {
        this.repository = repository;
        this.cpfService = cpfService;
    }

    @Override
    public List<Student> findAll() {
        return repository.findAll();
    }

    @Override
    public Student findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
    }

    @Override
    public Student save(Student student) {
        cpfService.checkIfExistsWithCpf(student.getCpf(), null);
        return repository.save(student);
    }

    @Override
    public Student update(Long id, Student student) {
        var studentToUpdate = findById(id);
        cpfService.checkIfExistsWithCpf(student.getCpf(), studentToUpdate.getCpf());
        studentToUpdate.setName(student.getName());
        studentToUpdate.setCpf(student.getCpf());
        return repository.save(studentToUpdate);
    }

    @Override
    public void delete(Long id) {
        var studentExists = findById(id);
        repository.delete(studentExists);
    }
}
