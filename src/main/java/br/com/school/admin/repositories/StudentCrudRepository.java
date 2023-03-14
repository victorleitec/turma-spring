package br.com.school.admin.repositories;

import br.com.school.admin.models.Student;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentCrudRepository extends DefaultCrudRepository<Student> {
}
