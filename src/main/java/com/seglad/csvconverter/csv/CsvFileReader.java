package com.seglad.csvconverter.csv;

import com.seglad.csvconverter.model.Person;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class CsvFileReader {

    private final CsvLineParser lineParser;

    public CsvFileReader(CsvLineParser lineParser) {
        this.lineParser = lineParser;
    }

    public List<Person> readPeople(Path csvFile) throws IOException {
        CsvRecordAssembler assembler = new CsvRecordAssembler();
        try (BufferedReader reader = Files.newBufferedReader(csvFile, StandardCharsets.UTF_8)) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                try {
                    lineParser.parseLine(line).ifPresent(assembler::process);
                } catch (IllegalArgumentException | IllegalStateException ex) {
                    throw new IOException("Invalid row at line " + lineNumber + ": " + ex.getMessage(), ex);
                }
            }
        }
        try {
            return assembler.finish();
        } catch (IllegalStateException ex) {
            throw new IOException("Invalid file: " + ex.getMessage(), ex);
        }
    }
}
