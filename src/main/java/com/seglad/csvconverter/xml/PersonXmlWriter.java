package com.seglad.csvconverter.xml;

import com.seglad.csvconverter.model.Address;
import com.seglad.csvconverter.model.Family;
import com.seglad.csvconverter.model.Person;
import com.seglad.csvconverter.model.Phone;
import java.io.IOException;
import java.io.Writer;
import org.springframework.stereotype.Component;

@Component
public class PersonXmlWriter {

    public void writeDocument(Writer writer, Iterable<Person> people) throws IOException {
        writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        writer.write("<people>\n");
        for (Person person : people) {
            writePerson(writer, person);
        }
        writer.write("</people>\n");
    }

    private void writePerson(Writer writer, Person person) throws IOException {
        writer.write("  <person>\n");
        writeElement(writer, "firstname", person.firstname(), 4);
        writeElement(writer, "lastname", person.lastname(), 4);
        if (person.phone() != null) {
            writePhone(writer, person.phone(), 4);
        }
        if (person.address() != null) {
            writeAddress(writer, person.address(), 4);
        }
        if (person.family() != null) {
            writeFamily(writer, person.family(), 4);
        }
        writer.write("  </person>\n");
    }

    private void writeFamily(Writer writer, Family family, int indent) throws IOException {
        writer.write(indent(indent) + "<family>\n");
        writeElement(writer, "name", family.name(), indent + 2);
        writeElement(writer, "born", String.valueOf(family.born()), indent + 2);
        if (family.phone() != null) {
            writePhone(writer, family.phone(), indent + 2);
        }
        if (family.address() != null) {
            writeAddress(writer, family.address(), indent + 2);
        }
        writer.write(indent(indent) + "</family>\n");
    }

    private void writePhone(Writer writer, Phone phone, int indent) throws IOException {
        writer.write(indent(indent) + "<phone>\n");
        writeElement(writer, "mobile", phone.mobile(), indent + 2);
        if (phone.landline() != null && !phone.landline().isBlank()) {
            writeElement(writer, "landline", phone.landline(), indent + 2);
        }
        writer.write(indent(indent) + "</phone>\n");
    }

    private void writeAddress(Writer writer, Address address, int indent) throws IOException {
        writer.write(indent(indent) + "<address>\n");
        writeElement(writer, "street", address.street(), indent + 2);
        writeElement(writer, "city", address.city(), indent + 2);
        if (address.zip() != null) {
            writeElement(writer, "zip", address.zip(), indent + 2);
        }
        writer.write(indent(indent) + "</address>\n");
    }

    private void writeElement(Writer writer, String name, String value, int indent) throws IOException {
        writer.write(indent(indent) + "<" + name + ">");
        writer.write(XmlEscaper.escape(value));
        writer.write("</" + name + ">\n");
    }

    private static String indent(int spaces) {
        return " ".repeat(spaces);
    }
}
