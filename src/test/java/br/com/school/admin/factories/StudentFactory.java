package br.com.school.admin.factories;

import br.com.school.admin.models.Student;

import java.util.Arrays;
import java.util.List;

public class StudentFactory {

    public static Student createStudent() {
        return new Student("Joseph", "23759841023");
    }

    public static List<Student> createListOfStudents() {
        var s1 = new Student("Joseph", "74539808010");
        s1.setId(1L);
        var s2 = new Student("John", "40082430039");
        s2.setId(2L);
        return Arrays.asList(s1, s2);
    }

    public static Student createStudentWithInvalidCpf() {
        return new Student("Joseph", "invalid_cpf");
    }

    public static Student createStudentWithEmptyName() {
        return new Student("", "33635261050");
    }

    public static Student createStudentWithEmptyCpf() {
        return new Student("Joseph", "");
    }

    public static Student createStudentWithCpfAlreadyRegistered() {
        return new Student("Joseph", "40082430039");
    }
}
