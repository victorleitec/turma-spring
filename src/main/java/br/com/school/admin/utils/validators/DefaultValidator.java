package br.com.school.admin.utils.validators;

import br.com.school.admin.models.Director;
import br.com.school.admin.models.Student;
import br.com.school.admin.models.Teacher;

public class DefaultValidator {

    private DefaultValidator() {
    }

    public static void isValidName(String name) {
        if (name == null || name.isEmpty() || name.isBlank()) {
            throw new IllegalArgumentException("Name is required");
        }
    }

    public static void isValidCpf(String cpf) {
        if (cpf == null || cpf.isEmpty() || cpf.isBlank()) {
            throw new IllegalArgumentException("CPF is required");
        }

        if (!cpf.equals("00000000000") && !cpf.equals("11111111111")
                && !cpf.equals("22222222222") && !cpf.equals("33333333333")
                && !cpf.equals("44444444444") && !cpf.equals("55555555555")
                && !cpf.equals("66666666666") && !cpf.equals("77777777777")
                && !cpf.equals("88888888888") && !cpf.equals("99999999999")
                && (cpf.length() == 11)) {
            char dig10, dig11;
            int sm, i, r, num, peso;
            sm = 0;
            peso = 10;
            for (i = 0; i < 9; i++) {
                num = (cpf.charAt(i) - 48);
                sm = sm + (num * peso);
                peso = peso - 1;
            }
            r = 11 - (sm % 11);
            if ((r == 10) || (r == 11)) {
                dig10 = '0';
            } else {
                dig10 = (char) (r + 48);
            }
            sm = 0;
            peso = 11;
            for (i = 0; i < 10; i++) {
                num = (cpf.charAt(i) - 48);
                sm = sm + (num * peso);
                peso = peso - 1;
            }
            r = 11 - (sm % 11);
            if ((r == 10) || (r == 11)) {
                dig11 = '0';
            } else {
                dig11 = (char) (r + 48);
            }
            if ((dig10 != cpf.charAt(9)) || (dig11 != cpf.charAt(10))) {
                throw new IllegalArgumentException("CPF is invalid");
            }
        }
    }

    public static void isValidSpecialty(String specialty) {
        if (specialty == null || specialty.isEmpty() || specialty.isBlank()) {
            throw new IllegalArgumentException("Specialty is required");
        }
    }

    public static void isValidStudent(Student student) {
        isValidName(student.getName());
        isValidCpf(student.getCpf());
    }

    public static void isValidTeacher(Teacher teacher) {
        isValidName(teacher.getName());
        isValidCpf(teacher.getCpf());
        isValidSpecialty(teacher.getSpecialty());
    }

    public static void isValidDirector(Director director) {
        isValidName(director.getName());
        isValidCpf(director.getCpf());
    }
}
