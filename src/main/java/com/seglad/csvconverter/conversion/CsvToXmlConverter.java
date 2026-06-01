package com.seglad.csvconverter.conversion;

import java.io.IOException;
import java.nio.file.Path;

public interface CsvToXmlConverter {

    /**
     * Converts the given CSV file to XML. Implementations write XML to a temporary file
     * and return its path; the integration flow moves the result to the output directory.
     */
    Path convert(Path csvFile) throws IOException;
}
