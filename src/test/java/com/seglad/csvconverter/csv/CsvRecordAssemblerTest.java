package com.seglad.csvconverter.csv;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.seglad.csvconverter.model.Address;
import com.seglad.csvconverter.model.Family;
import com.seglad.csvconverter.model.Person;
import com.seglad.csvconverter.model.Phone;
import org.junit.jupiter.api.Test;

class CsvRecordAssemblerTest {

  private final CsvLineParser lineParser = new CsvLineParser();

  @Test
  void fullPersonWithMultipleFamiliesAndAddresses() {
    CsvRecordAssembler assembler = new CsvRecordAssembler();
    process(assembler, "P|Ada|Lovelace");
    process(assembler, "T|0701|08-1");
    process(assembler, "A|Street|City|12345");
    process(assembler, "F|Lovelace|1815");
    process(assembler, "T|0702|08-2");
    process(assembler, "A|Family St|Family City|99999");
    process(assembler, "F|Byron|1788");

    Person person = assembler.finish().getFirst();
    assertThat(person.firstname()).isEqualTo("Ada");
    assertThat(person.phone()).isEqualTo(new Phone("0701", "08-1"));
    assertThat(person.addresses()).containsExactly(new Address("Street", "City", "12345"));
    assertThat(person.families())
        .extracting(Family::name)
        .containsExactly("Lovelace", "Byron");
    assertThat(person.families().get(0).phone()).isEqualTo(new Phone("0702", "08-2"));
    assertThat(person.families().get(0).addresses())
        .containsExactly(new Address("Family St", "Family City", "99999"));
    assertThat(person.families().get(1).addresses()).isEmpty();
  }

  @Test
  void personOnlyWithPhoneAndAddress() {
    CsvRecordAssembler assembler = new CsvRecordAssembler();
    process(assembler, "P|Grace|Hopper");
    process(assembler, "A|Street|City|1");
    process(assembler, "T|0703|");

    Person person = assembler.finish().getFirst();
    assertThat(person.families()).isEmpty();
    assertThat(person.phone()).isEqualTo(new Phone("0703", null));
    assertThat(person.addresses()).containsExactly(new Address("Street", "City", "1"));
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
  void familyContextEndsWhenNextPersonStarts() {
    CsvRecordAssembler assembler = new CsvRecordAssembler();
    process(assembler, "P|Ada|Lovelace");
    process(assembler, "F|Lovelace|1815");
    process(assembler, "A|London St|London|11111");
    process(assembler, "P|Grace|Hopper");
    process(assembler, "A|Arlington St|Arlington|22222");
    process(assembler, "F|Hopper|1906");

    var people = assembler.finish();
    assertThat(people).extracting(Person::firstname).containsExactly("Ada", "Grace");
    assertThat(people.get(0).addresses()).isEmpty();
    assertThat(people.get(0).families()).extracting(Family::name).containsExactly("Lovelace");
    assertThat(people.get(0).families().getFirst().addresses())
        .containsExactly(new Address("London St", "London", "11111"));
    assertThat(people.get(1).addresses())
        .containsExactly(new Address("Arlington St", "Arlington", "22222"));
    assertThat(people.get(1).families()).extracting(Family::name).containsExactly("Hopper");
  }

  @Test
  void rowsAfterFamilyAttachToThatFamilyUntilNextFamilyOrPerson() {
    CsvRecordAssembler assembler = new CsvRecordAssembler();
    process(assembler, "P|Ada|Lovelace");
    process(assembler, "F|Lovelace|1815");
    process(assembler, "A|After Family|London|");
    process(assembler, "T|0701|");
    process(assembler, "F|Byron|1788");
    process(assembler, "A|After Phone|Oxford|");

    Person person = assembler.finish().getFirst();
    assertThat(person.phone()).isNull();
    assertThat(person.addresses()).isEmpty();
    assertThat(person.families())
        .extracting(Family::name)
        .containsExactly("Lovelace", "Byron");
    assertThat(person.families().get(0).phone()).isEqualTo(new Phone("0701", null));
    assertThat(person.families().get(0).addresses())
        .containsExactly(new Address("After Family", "London", null));
    assertThat(person.families().get(1).addresses())
        .containsExactly(new Address("After Phone", "Oxford", null));
  }

  private void process(CsvRecordAssembler assembler, String line) {
    lineParser.parseLine(line).ifPresent(assembler::process);
  }
}
