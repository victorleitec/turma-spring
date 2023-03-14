package br.com.school.admin.models;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;

@Entity
@Table(name = "tb_students")
@Validated
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cd_student")
    private Long id;

    @Column(name = "nm_student")
    @NotNull(message = "Name is required")
    @NotEmpty(message = "Name is required")
    private String name;

    @Column(name = "nr_cpf")
    @NotNull(message = "CPF is required")
    @NotEmpty(message = "CPF is required")
    @Length(min = 11, max = 11, message = "CPF must have 11 digits")
    private String cpf;

    public Student(String name, String cpf) {
        this.name = name;
        this.cpf = cpf;
    }

    public Student() {
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
