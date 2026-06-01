package com.seglad.csvconverter.csv;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.seglad.csvconverter.model.Address;
import com.seglad.csvconverter.model.Person;
import com.seglad.csvconverter.model.Phone;
import org.junit.jupiter.api.Test;

class CsvRecordAssemblerTest {

  private final CsvLineParser lineParser = new CsvLineParser();

  @Test
  void fullPersonWithFamilyAndNestedContact() {
    CsvRecordAssembler assembler = new CsvRecordAssembler();
    process(assembler, "P|Ada|Lovelace");
    process(assembler, "T|0701|08-1");
    process(assembler, "A|Street|City|12345");
    process(assembler, "F|Lovelace|1815");
    process(assembler, "T|0702|08-2");
    process(assembler, "A|Family St|Family City|99999");

    Person person = assembler.finish().getFirst();
    assertThat(person.firstname()).isEqualTo("Ada");
    assertThat(person.phone()).isEqualTo(new Phone("0701", "08-1"));
    assertThat(person.address()).isEqualTo(new Address("Street", "City", "12345"));
    assertThat(person.family().name()).isEqualTo("Lovelace");
    assertThat(person.family().phone()).isEqualTo(new Phone("0702", "08-2"));
    assertThat(person.family().address()).isEqualTo(new Address("Family St", "Family City", "99999"));
  }

  @Test
  void personOnlyWithPhoneAndAddress() {
    CsvRecordAssembler assembler = new CsvRecordAssembler();
    process(assembler, "P|Grace|Hopper");
    process(assembler, "A|Street|City|1");
    process(assembler, "T|0703|");

    Person person = assembler.finish().getFirst();
    assertThat(person.family()).isNull();
    assertThat(person.phone().mobile()).isEqualTo("0703");
    assertThat(person.address().city()).isEqualTo("City");
  }

  @Test
  void multiplePeople() {
    CsvRecordAssembler assembler = new CsvRecordAssembler();
    process(assembler, "P|Ada|Lovelace");
    process(assembler, "P|Grace|Hopper");

    assertThat(assembler.finish()).hasSize(2);
  }

  @Test
  void rejectsPhoneBeforePerson() {
    CsvRecordAssembler assembler = new CsvRecordAssembler();
    assertThatThrownBy(() -> process(assembler, "T|0701|08-1"))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("must follow a person");
  }

  @Test
  void rejectsFamilyBeforePerson() {
    CsvRecordAssembler assembler = new CsvRecordAssembler();
    assertThatThrownBy(() -> process(assembler, "F|Smith|1990"))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("must follow a person");
  }

  @Test
  void rejectsDuplicatePersonPhone() {
    CsvRecordAssembler assembler = new CsvRecordAssembler();
    process(assembler, "P|Ada|Lovelace");
    process(assembler, "T|0701|");
    assertThatThrownBy(() -> process(assembler, "T|0702|"))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Duplicate phone");
  }

  @Test
  void rejectsDuplicateFamily() {
    CsvRecordAssembler assembler = new CsvRecordAssembler();
    process(assembler, "P|Ada|Lovelace");
    process(assembler, "F|Lovelace|1815");
    assertThatThrownBy(() -> process(assembler, "F|Other|1900"))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("already has a family");
  }

  @Test
  void familyPhoneAppliesToFamilyNotPerson() {
    CsvRecordAssembler assembler = new CsvRecordAssembler();
    process(assembler, "P|Ada|Lovelace");
    process(assembler, "F|Lovelace|1815");
    process(assembler, "T|family-mobile|");

    Person person = assembler.finish().getFirst();
    assertThat(person.phone()).isNull();
    assertThat(person.family().phone().mobile()).isEqualTo("family-mobile");
  }

  private void process(CsvRecordAssembler assembler, String line) {
    lineParser.parseLine(line).ifPresent(assembler::process);
  }
}
