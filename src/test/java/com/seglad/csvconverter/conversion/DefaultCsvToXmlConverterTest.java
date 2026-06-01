package com.seglad.csvconverter.conversion;

import static org.assertj.core.api.Assertions.assertThat;

import com.seglad.csvconverter.csv.CsvFileReader;
import com.seglad.csvconverter.csv.CsvLineParser;
import com.seglad.csvconverter.xml.PersonXmlWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DefaultCsvToXmlConverterTest {

  @TempDir Path tempDir;

  private final DefaultCsvToXmlConverter converter =
      new DefaultCsvToXmlConverter(new CsvFileReader(new CsvLineParser()), new PersonXmlWriter());

  @Test
  void convert_writesNestedPersonXml() throws Exception {
    Path csv = tempDir.resolve("people.csv");
    Files.writeString(
        csv,
        """
        P|Ada|Lovelace
        T|0701111111|08-111
        A|Person St|London|11111
        F|Lovelace|1815
        T|0702222222|08-222
        A|Family St|Oxford|22222
        """,
        StandardCharsets.UTF_8);

    String xml = Files.readString(converter.convert(csv), StandardCharsets.UTF_8);
    assertThat(xml).contains("<firstname>Ada</firstname>");
    assertThat(xml).contains("<mobile>0701111111</mobile>");
    assertThat(xml).contains("<street>Person St</street>");
    assertThat(xml).contains("<family>");
    assertThat(xml).contains("<name>Lovelace</name>");
    assertThat(xml).contains("<born>1815</born>");
    assertThat(xml).contains("<mobile>0702222222</mobile>");
    assertThat(xml).contains("<street>Family St</street>");
  }

  @Test
  void convert_escapesXmlInNames() throws Exception {
    Path csv = tempDir.resolve("people.csv");
    Files.writeString(csv, "P|Tom|O'Brien\n", StandardCharsets.UTF_8);

    String xml = Files.readString(converter.convert(csv), StandardCharsets.UTF_8);
    assertThat(xml).contains("<lastname>O&apos;Brien</lastname>");
  }
}
