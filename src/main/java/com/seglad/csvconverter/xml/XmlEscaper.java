package com.seglad.csvconverter.xml;

public final class XmlEscaper {

    private XmlEscaper() {}

    public static String escape(String value) {
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}
