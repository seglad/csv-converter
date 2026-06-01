package com.seglad.csvconverter.csv;

import com.seglad.csvconverter.model.Address;
import com.seglad.csvconverter.model.Family;
import com.seglad.csvconverter.model.Person;
import com.seglad.csvconverter.model.Phone;
import java.util.ArrayList;
import java.util.List;

public class CsvRecordAssembler {

    private enum Context {
        AWAITING_PERSON,
        PERSON_LEVEL,
        FAMILY_LEVEL
    }

    private final List<Person> people = new ArrayList<>();
    private Context context = Context.AWAITING_PERSON;
    private PersonBuilder currentPerson;

    public void process(ParsedRow row) {
        switch (row) {
            case ParsedRow.PersonRow person -> handlePerson(person);
            case ParsedRow.PhoneRow phone -> handlePhone(phone.phone());
            case ParsedRow.AddressRow address -> handleAddress(address.address());
            case ParsedRow.FamilyRow family -> handleFamily(family.family());
        }
    }

    public List<Person> finish() {
        completeCurrentPerson();
        if (people.isEmpty()) {
            throw new IllegalStateException("File must contain at least one person (P row)");
        }
        return List.copyOf(people);
    }

    private void handlePerson(ParsedRow.PersonRow person) {
        completeCurrentPerson();
        currentPerson = new PersonBuilder(person.firstname(), person.lastname());
        context = Context.PERSON_LEVEL;
    }

    private void handlePhone(Phone phone) {
        switch (context) {
            case AWAITING_PERSON -> throw invalidOrder("Phone row (T) must follow a person (P)");
            case PERSON_LEVEL -> {
                if (currentPerson.phone != null) {
                    throw duplicate("phone", "person");
                }
                currentPerson.phone = phone;
            }
            case FAMILY_LEVEL -> {
                FamilyBuilder family = requireFamily();
                if (family.phone != null) {
                    throw duplicate("phone", "family");
                }
                family.phone = phone;
            }
        }
    }

    private void handleAddress(Address address) {
        switch (context) {
            case AWAITING_PERSON -> throw invalidOrder("Address row (A) must follow a person (P)");
            case PERSON_LEVEL -> currentPerson.addresses.add(address);
            case FAMILY_LEVEL -> requireFamily().addresses.add(address);
        }
    }

    private void handleFamily(Family familyHeader) {
        if (context == Context.AWAITING_PERSON) {
            throw invalidOrder("Family row (F) must follow a person (P)");
        }
        FamilyBuilder family = new FamilyBuilder(familyHeader.name(), familyHeader.born());
        currentPerson.families.add(family);
        context = Context.FAMILY_LEVEL;
    }

    private FamilyBuilder requireFamily() {
        if (currentPerson.families.isEmpty()) {
            throw new IllegalStateException("Family row (F) must appear before family phone or address rows");
        }
        return currentPerson.families.getLast();
    }

    private void completeCurrentPerson() {
        if (currentPerson == null) {
            return;
        }
        people.add(currentPerson.build());
        currentPerson = null;
        context = Context.AWAITING_PERSON;
    }

    private static IllegalStateException invalidOrder(String message) {
        return new IllegalStateException(message);
    }

    private static IllegalStateException duplicate(String entity, String owner) {
        return new IllegalStateException("Duplicate %s row for the same %s".formatted(entity, owner));
    }

    private static final class PersonBuilder {
        private final String firstname;
        private final String lastname;
        private Phone phone;
        private final List<Address> addresses = new ArrayList<>();
        private final List<FamilyBuilder> families = new ArrayList<>();

        private PersonBuilder(String firstname, String lastname) {
            this.firstname = firstname;
            this.lastname = lastname;
        }

        private Person build() {
            return new Person(firstname, lastname, phone, addresses, families.stream().map(FamilyBuilder::build).toList());
        }
    }

    private static final class FamilyBuilder {
        private final String name;
        private final int born;
        private Phone phone;
        private final List<Address> addresses = new ArrayList<>();

        private FamilyBuilder(String name, int born) {
            this.name = name;
            this.born = born;
        }

        private Family build() {
            return new Family(name, born, phone, addresses);
        }
    }
}
