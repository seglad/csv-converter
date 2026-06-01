package com.seglad.csvconverter.csv;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.seglad.csvconverter.model.Address;
import com.seglad.csvconverter.model.Family;
import com.seglad.csvconverter.model.Phone;
import org.junit.jupiter.api.Test;

class CsvLineParserTest {

  private final CsvLineParser parser = new CsvLineParser();

  @Test
  void parseLine_parsesPersonRow() {
    assertThat(parser.parseLine("P|Ada|Lovelace"))
        .contains(new ParsedRow.PersonRow("Ada", "Lovelace"));
  }

  @Test
  void parseLine_parsesPhoneRow() {
    assertThat(parser.parseLine("T|0701234567|08-123456"))
        .contains(new ParsedRow.PhoneRow(new Phone("0701234567", "08-123456")));
  }

  @Test
  void parseLine_parsesAddressRow() {
    assertThat(parser.parseLine("A|Main St|London|SW1A1AA"))
        .contains(new ParsedRow.AddressRow(new Address("Main St", "London", "SW1A1AA")));
  }

  @Test
  void parseLine_parsesFamilyRow() {
    assertThat(parser.parseLine("F|Smith|1990"))
        .contains(new ParsedRow.FamilyRow(new Family("Smith", 1990, null, null)));
  }

  @Test
  void parseLine_rejectsUnknownRowType() {
    assertThatThrownBy(() -> parser.parseLine("X|a|b"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Unknown row type");
  }
}
