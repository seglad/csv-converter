package com.seglad.csvconverter.csv;

import com.seglad.csvconverter.model.Address;
import com.seglad.csvconverter.model.Family;
import com.seglad.csvconverter.model.Phone;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class CsvLineParser {

    public Optional<ParsedRow> parseLine(String line) {
        if (line == null) {
            return Optional.empty();
        }

        String trimmed = line.trim();
        if (trimmed.isEmpty()) {
            return Optional.empty();
        }

        String[] fields = trimmed.split("\\|", -1);
        String rowType = fields[0].trim();
        return switch (rowType) {
            case "P" -> Optional.of(parsePerson(fields, line));
            case "T" -> Optional.of(parsePhone(fields, line));
            case "A" -> Optional.of(parseAddress(fields, line));
            case "F" -> Optional.of(parseFamily(fields, line));
            default -> throw new IllegalArgumentException("Unknown row type '%s': %s".formatted(rowType, line));
        };
    }

    private ParsedRow.PersonRow parsePerson(String[] fields, String line) {
        if (fields.length != 3) {
            throw new IllegalArgumentException(
                    "Person row must have exactly 3 pipe-separated fields (P|firstname|lastname): " + line);
        }
        String firstname = fields[1].trim();
        String lastname = fields[2].trim();
        if (firstname.isEmpty() || lastname.isEmpty()) {
            throw new IllegalArgumentException("Person firstname and lastname must not be empty: " + line);
        }
        return new ParsedRow.PersonRow(firstname, lastname);
    }

    private ParsedRow.PhoneRow parsePhone(String[] fields, String line) {
        if (fields.length != 3) {
            throw new IllegalArgumentException(
                    "Phone row must have exactly 3 pipe-separated fields (T|mobile|landline): " + line);
        }
        String mobile = fields[1].trim();
        if (mobile.isEmpty()) {
            throw new IllegalArgumentException("Phone mobile must not be empty: " + line);
        }
        String landline = fields[2].trim();
        String landlineOrNull = landline.isEmpty() ? null : landline;
        return new ParsedRow.PhoneRow(new Phone(mobile, landlineOrNull));
    }

    private ParsedRow.AddressRow parseAddress(String[] fields, String line) {
        if (fields.length < 3 || fields.length > 4) {
            throw new IllegalArgumentException(
                    "Address row must have 3 or 4 pipe-separated fields (A|street|city or A|street|city|zip): "
                            + line);
        }
        String street = fields[1].trim();
        String city = fields[2].trim();
        if (street.isEmpty() || city.isEmpty()) {
            throw new IllegalArgumentException("Address street and city must not be empty: " + line);
        }
        String zipOrNull = null;
        if (fields.length == 4) {
            String zip = fields[3].trim();
            if (!zip.isEmpty()) {
                zipOrNull = zip;
            }
        }
        return new ParsedRow.AddressRow(new Address(street, city, zipOrNull));
    }

    private ParsedRow.FamilyRow parseFamily(String[] fields, String line) {
        if (fields.length != 3) {
            throw new IllegalArgumentException(
                    "Family row must have exactly 3 pipe-separated fields (F|name|born): " + line);
        }
        String name = fields[1].trim();
        String bornText = fields[2].trim();
        if (name.isEmpty()) {
            throw new IllegalArgumentException("Family name must not be empty: " + line);
        }
        try {
            int born = Integer.parseInt(bornText);
            return new ParsedRow.FamilyRow(new Family(name, born, null, List.of()));
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Family born year must be a valid integer: " + line, ex);
        }
    }
}
