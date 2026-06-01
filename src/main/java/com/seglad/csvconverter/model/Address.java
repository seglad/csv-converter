package com.seglad.csvconverter.model;

public record Address(String street, String city, String zip) {

    public Address {
        if (zip != null && zip.isBlank()) {
            zip = null;
        }
    }
}
