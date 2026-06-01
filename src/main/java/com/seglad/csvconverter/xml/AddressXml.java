package com.seglad.csvconverter.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.FIELD)
public class AddressXml {

    private String street;
    private String city;
    private String zip;

    public AddressXml() {}

    public AddressXml(String street, String city, String zip) {
        this.street = street;
        this.city = city;
        this.zip = zip;
    }
}
