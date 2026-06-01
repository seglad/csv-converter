package com.seglad.csvconverter.model;

import java.util.List;

public record Family(String name, int born, Phone phone, List<Address> addresses) {

    public Family {
        addresses = List.copyOf(addresses);
    }
}
