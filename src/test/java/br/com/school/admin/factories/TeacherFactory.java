package br.com.school.admin.factories;

import br.com.school.admin.models.Teacher;

import java.util.Arrays;
import java.util.List;

public class TeacherFactory {

    public static Teacher createTeacher() {
        return new Teacher("Joseph", "23759841023", "Math");
    }

    public static List<Teacher> createListOfTeachers() {
        var t1 = new Teacher("Joseph", "74539808010", "Math");
        t1.setId(1L);
        var t2 = new Teacher("John", "40082430039", "Portuguese");
        t2.setId(2L);
        return Arrays.asList(t1, t2);
    }

    public static Teacher createTeacherWithInvalidCpf() {
        return new Teacher("Joseph", "invalid_cpf", "Math");
    }

    public static Teacher createTeacherWithEmptyName() {
        return new Teacher("", "33635261050", "Math");
    }

    public static Teacher createTeacherWithEmptyCpf() {
        return new Teacher("Joseph", "", "Math");
    }

    public static Teacher createTeacherWithEmptySpecialty() {
        return new Teacher("Joseph", "40082430039", "");
    }

    public static Teacher createTeacherWithCpfAlreadyRegistered() {
        return new Teacher("Joseph", "40082430039", "Math");
    }
}
