package com.seglad.csvconverter.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
public class FamilyXml {

    private String name;
    private int born;
    private PhoneXml phone;

    @XmlElement(name = "address")
    private List<AddressXml> addresses = new ArrayList<>();

    public FamilyXml() {}

    public FamilyXml(String name, int born, PhoneXml phone, List<AddressXml> addresses) {
        this.name = name;
        this.born = born;
        this.phone = phone;
        this.addresses = addresses;
    }
}
