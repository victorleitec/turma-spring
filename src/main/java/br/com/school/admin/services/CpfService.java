package br.com.school.admin.services;

import br.com.school.admin.exceptions.BusinessRuleException;
import br.com.school.admin.repositories.DirectorCrudRepository;
import br.com.school.admin.repositories.StudentCrudRepository;
import br.com.school.admin.repositories.TeacherCrudRepository;
import org.springframework.stereotype.Service;

@Service
public class CpfService {

    private final StudentCrudRepository studentRepository;
    private final TeacherCrudRepository teacherRepository;
    private final DirectorCrudRepository directorRepository;

    public CpfService(StudentCrudRepository studentRepository, TeacherCrudRepository teacherRepository, DirectorCrudRepository directorRepository) {
        this.studentRepository = studentRepository;
        this.teacherRepository = teacherRepository;
        this.directorRepository = directorRepository;
    }

    public void existsByCpfAndDifferentThanCurrentCpf(String cpf, String currentCpf) {
        var studentExists = studentRepository.existsByCpf(cpf);
        var teacherExists = teacherRepository.existsByCpf(cpf);
        var directorExists = directorRepository.existsByCpf(cpf);

        if ((studentExists || teacherExists || directorExists) && !cpf.equals(currentCpf)) {
            throw new BusinessRuleException("Cpf already exists");
        }
    }
}
