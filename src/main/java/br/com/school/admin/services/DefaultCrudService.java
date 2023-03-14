package br.com.school.admin.services;

import java.util.List;

public interface DefaultCrudService<T> {

    List<T> findAll();

    T findById(Long id);

    T save(T t);

    T update(Long id, T t);

    void delete(Long id);
}
