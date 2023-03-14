package br.com.school.admin.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface DefaultCrudRepository<T> extends JpaRepository<T, Long> {
    boolean existsByCpf(String cpf);
}
