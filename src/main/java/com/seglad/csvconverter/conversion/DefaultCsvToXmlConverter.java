package com.seglad.csvconverter.conversion;

import com.seglad.csvconverter.csv.CsvFileReader;
import com.seglad.csvconverter.model.Person;
import com.seglad.csvconverter.xml.PersonXmlWriter;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class DefaultCsvToXmlConverter implements CsvToXmlConverter {

    private final CsvFileReader csvFileReader;
    private final PersonXmlWriter personXmlWriter;

    public DefaultCsvToXmlConverter(CsvFileReader csvFileReader, PersonXmlWriter personXmlWriter) {
        this.csvFileReader = csvFileReader;
        this.personXmlWriter = personXmlWriter;
    }

    @Override
    public Path convert(Path csvFile) throws IOException {
        List<Person> people = csvFileReader.readPeople(csvFile);

        String baseName = stripExtension(csvFile.getFileName().toString());
        Path tempFile = Files.createTempFile("csv-convert-" + baseName + "-", ".xml");
        try (BufferedWriter writer = Files.newBufferedWriter(tempFile, StandardCharsets.UTF_8)) {
            personXmlWriter.writeDocument(writer, people);
        }
        return tempFile;
    }

    private static String stripExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        return dot > 0 ? filename.substring(0, dot) : filename;
    }
}
