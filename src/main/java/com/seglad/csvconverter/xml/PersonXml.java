package com.seglad.csvconverter.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
public class PersonXml {

    private String firstname;
    private String lastname;
    private PhoneXml phone;

    @XmlElement(name = "address")
    private List<AddressXml> addresses = new ArrayList<>();

    @XmlElement(name = "family")
    private List<FamilyXml> families = new ArrayList<>();

    public PersonXml() {}

    public PersonXml(
            String firstname, String lastname, PhoneXml phone, List<AddressXml> addresses, List<FamilyXml> families) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.phone = phone;
        this.addresses = addresses;
        this.families = families;
    }
}
