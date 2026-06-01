package com.seglad.csvconverter.model;

public record Phone(String mobile, String landline) {

    public Phone {
        if (landline != null && landline.isBlank()) {
            landline = null;
        }
    }
}
