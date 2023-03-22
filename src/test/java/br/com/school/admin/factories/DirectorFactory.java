package br.com.school.admin.factories;

import br.com.school.admin.models.Director;

import java.util.Arrays;
import java.util.List;

public class DirectorFactory {

    public static Director createDirector() {
        return new Director("Joseph", "23759841023");
    }

    public static List<Director> createListOfDirectors() {
        var d1 = new Director("Joseph", "74539808010");
        d1.setId(1L);
        var d2 = new Director("John", "40082430039");
        d2.setId(2L);
        return Arrays.asList(d1, d2);
    }

    public static Director createDirectorWithInvalidCpf() {
        return new Director("Joseph", "invalid_cpf");
    }

    public static Director createDirectorWithEmptyName() {
        return new Director("", "33635261050");
    }

    public static Director createDirectorWithEmptyCpf() {
        return new Director("Joseph", "");
    }

    public static Director createDirectorWithCpfAlreadyRegistered() {
        return new Director("Joseph", "40082430039");
    }
}
