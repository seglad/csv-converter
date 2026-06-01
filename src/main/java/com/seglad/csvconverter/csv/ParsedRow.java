package com.seglad.csvconverter.csv;

import com.seglad.csvconverter.model.Address;
import com.seglad.csvconverter.model.Family;
import com.seglad.csvconverter.model.Phone;

public sealed interface ParsedRow {

    record PersonRow(String firstname, String lastname) implements ParsedRow {}

    record PhoneRow(Phone phone) implements ParsedRow {}

    record AddressRow(Address address) implements ParsedRow {}

    record FamilyRow(Family family) implements ParsedRow {}
}
