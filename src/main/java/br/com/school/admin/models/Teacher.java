package br.com.school.admin.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

@Entity
@Table(name = "tb_teachers")
@Validated
public class Teacher {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cd_teacher")
    private Long id;

    @Column(name = "nm_teacher")
    @NotNull(message = "Name is required")
    @NotEmpty(message = "Name is required")
    private String name;

    @Column(name = "nr_cpf")
    @NotNull(message = "CPF is required")
    @NotEmpty(message = "CPF is required")
    private String cpf;

    @Column(name = "ds_specialty")
    @NotNull(message = "Specialty is required")
    @NotEmpty(message = "Specialty is required")
    private String specialty;

    public Teacher(String name, String cpf, String specialty) {
        this.name = name;
        this.cpf = cpf;
        this.specialty = specialty;
    }

    public Teacher() {
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

    public String getSpecialty() {
        return specialty;
    }

    public void setSpecialty(String specialty) {
        this.specialty = specialty;
    }
}
