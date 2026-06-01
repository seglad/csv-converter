package com.seglad.csvconverter.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest
class FileConversionFlowIT {

  @TempDir static Path tempRoot;

  static Path inputDir;
  static Path outputDir;
  static Path errorDir;

  @DynamicPropertySource
  static void overrideProperties(DynamicPropertyRegistry registry) {
    inputDir = tempRoot.resolve("input");
    outputDir = tempRoot.resolve("output");
    errorDir = tempRoot.resolve("error");

    registry.add("csv-converter.input-directory", () -> inputDir.toString());
    registry.add("csv-converter.output-directory", () -> outputDir.toString());
    registry.add("csv-converter.error-directory", () -> errorDir.toString());
    registry.add("csv-converter.poll-interval-ms", () -> "200");
  }

  @BeforeEach
  void setUp() throws Exception {
    Files.createDirectories(inputDir);
    Files.createDirectories(outputDir);
    Files.createDirectories(errorDir);

    try (Stream<Path> outputs = Files.list(outputDir)) {
      outputs.forEach(
          path -> {
            try {
              Files.deleteIfExists(path);
            } catch (Exception ignored) {
              // best effort cleanup between tests
            }
          });
    }
  }

  @Test
  void droppedCsv_isConvertedToXmlInOutputDirectory() throws Exception {
    Path csv = inputDir.resolve("sample.csv");
    Files.writeString(
        csv,
        """
        P|Ada|Lovelace
        T|0701111111|
        """,
        StandardCharsets.UTF_8);

    await().atMost(Duration.ofSeconds(10))
        .pollInterval(Duration.ofMillis(200))
        .until(() -> Files.exists(outputDir.resolve("sample.xml")));

    Path xml = outputDir.resolve("sample.xml");
    String content = Files.readString(xml, StandardCharsets.UTF_8);
    assertThat(content).contains("<people>");
    assertThat(content).contains("<firstname>Ada</firstname>");
    assertThat(content).contains("<lastname>Lovelace</lastname>");
    assertThat(content).contains("<mobile>0701111111</mobile>");

    try (Stream<Path> remaining = Files.list(inputDir)) {
      assertThat(remaining.filter(p -> p.getFileName().toString().contains("sample")))
          .isEmpty();
    }
  }
}
