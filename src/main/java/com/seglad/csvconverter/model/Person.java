package com.seglad.csvconverter.model;

import java.util.List;

public record Person(
        String firstname, String lastname, Phone phone, List<Address> addresses, List<Family> families) {

    public Person {
        addresses = List.copyOf(addresses);
        families = List.copyOf(families);
    }
}
