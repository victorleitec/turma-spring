package br.com.school.admin.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "tb_directors")
public class Director {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cd_director")
    private Long id;

    @Column(name = "nm_director")
    @NotEmpty(message = "Name is required")
    @NotNull(message = "Name is required")
    private String name;

    @Column(name = "nr_cpf")
    @NotEmpty(message = "CPF is required")
    @NotNull(message = "CPF is required")
    private String cpf;

    public Director(String name, String cpf) {
        this.name = name;
        this.cpf = cpf;
    }

    public Director() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }
}
