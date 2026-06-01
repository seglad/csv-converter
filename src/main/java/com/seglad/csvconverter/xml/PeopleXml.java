package com.seglad.csvconverter.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "people")
@XmlAccessorType(XmlAccessType.FIELD)
public class PeopleXml {

    @XmlElement(name = "person")
    private List<PersonXml> people = new ArrayList<>();

    public PeopleXml() {}

    public PeopleXml(List<PersonXml> people) {
        this.people = people;
    }
}
