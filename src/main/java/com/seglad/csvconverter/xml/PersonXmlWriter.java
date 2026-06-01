package com.seglad.csvconverter.xml;

import com.seglad.csvconverter.model.Address;
import com.seglad.csvconverter.model.Family;
import com.seglad.csvconverter.model.Person;
import com.seglad.csvconverter.model.Phone;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import java.io.IOException;
import java.io.Writer;
import java.util.stream.StreamSupport;
import org.springframework.stereotype.Component;

@Component
public class PersonXmlWriter {

    private final JAXBContext jaxbContext;

    public PersonXmlWriter() {
        try {
            this.jaxbContext = JAXBContext.newInstance(PeopleXml.class);
        } catch (JAXBException ex) {
            throw new IllegalStateException("Failed to initialize XML mapper", ex);
        }
    }

    public void writeDocument(Writer writer, Iterable<Person> people) throws IOException {
        try {
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(toXml(people), writer);
        } catch (JAXBException ex) {
            throw new IOException("Failed to write people XML", ex);
        }
    }

    private PeopleXml toXml(Iterable<Person> people) {
        return new PeopleXml(
                StreamSupport.stream(people.spliterator(), false).map(this::toXml).toList());
    }

    private PersonXml toXml(Person person) {
        return new PersonXml(
                person.firstname(),
                person.lastname(),
                toXml(person.phone()),
                person.addresses().stream().map(this::toXml).toList(),
                person.families().stream().map(this::toXml).toList());
    }

    private FamilyXml toXml(Family family) {
        return new FamilyXml(
                family.name(),
                family.born(),
                toXml(family.phone()),
                family.addresses().stream().map(this::toXml).toList());
    }

    private PhoneXml toXml(Phone phone) {
        return phone == null ? null : new PhoneXml(phone.mobile(), phone.landline());
    }

    private AddressXml toXml(Address address) {
        return new AddressXml(address.street(), address.city(), address.zip());
    }
}
