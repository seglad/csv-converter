package com.seglad.csvconverter.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.FIELD)
public class PhoneXml {

    private String mobile;
    private String landline;

    public PhoneXml() {}

    public PhoneXml(String mobile, String landline) {
        this.mobile = mobile;
        this.landline = landline;
    }
}
