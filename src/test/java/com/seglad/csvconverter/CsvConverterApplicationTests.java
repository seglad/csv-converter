package com.seglad.csvconverter;

import static org.assertj.core.api.Assertions.assertThat;

import com.seglad.csvconverter.config.VirtualThreadConfig;
import com.seglad.csvconverter.conversion.CsvToXmlConverter;
import com.seglad.csvconverter.conversion.DefaultCsvToXmlConverter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.task.TaskExecutor;
import org.springframework.integration.file.FileReadingMessageSource;
import org.springframework.core.task.support.TaskExecutorAdapter;

@SpringBootTest
class CsvConverterApplicationTests {

  @Autowired private CsvToXmlConverter csvToXmlConverter;

  @Autowired private FileReadingMessageSource csvFileSource;

  @Autowired
  @Qualifier(VirtualThreadConfig.CSV_CONVERTER_TASK_EXECUTOR)
  private TaskExecutor csvConverterTaskExecutor;

  @Test
  void contextLoads() {
    assertThat(csvToXmlConverter).isInstanceOf(DefaultCsvToXmlConverter.class);
    assertThat(csvFileSource).isNotNull();
    assertThat(csvConverterTaskExecutor).isInstanceOf(TaskExecutorAdapter.class);
  }
}
