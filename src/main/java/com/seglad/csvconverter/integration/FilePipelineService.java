package com.seglad.csvconverter.integration;

import com.seglad.csvconverter.conversion.CsvToXmlConverter;
import com.seglad.csvconverter.properties.ConverterProperties;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.file.FileHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Service
public class FilePipelineService {

    public static final String PROCESSING_FILE_HEADER = "processingFile";

    private static final Logger log = LoggerFactory.getLogger(FilePipelineService.class);

    private final ConverterProperties properties;
    private final CsvToXmlConverter converter;

    public FilePipelineService(ConverterProperties properties, CsvToXmlConverter converter) {
        this.properties = properties;
        this.converter = converter;
    }

    public File claim(File csvFile) throws IOException {
        Path claimed = csvFile.toPath().resolveSibling(csvFile.getName() + properties.getProcessingSuffix());
        return Files.move(csvFile.toPath(), claimed, StandardCopyOption.ATOMIC_MOVE).toFile();
    }

    public Message<byte[]> buildXmlMessage(File processingFile) throws IOException {
        Path tempXml = converter.convert(processingFile.toPath());
        try {
            String xmlFileName = toXmlFileName(processingFile.getName());
            byte[] content = Files.readAllBytes(tempXml);
            return MessageBuilder.withPayload(content)
                    .setHeader(FileHeaders.FILENAME, xmlFileName)
                    .setHeader(PROCESSING_FILE_HEADER, processingFile.getAbsolutePath())
                    .build();
        } finally {
            Files.deleteIfExists(tempXml);
        }
    }

    public void deleteProcessingFile(Message<?> message) {
        String processingPath = message.getHeaders().get(PROCESSING_FILE_HEADER, String.class);
        if (processingPath == null) {
            return;
        }
        try {
            Files.deleteIfExists(Path.of(processingPath));
        } catch (IOException ex) {
            log.warn("Failed to delete processing file {}", processingPath, ex);
        }
    }

    public void moveToErrorDirectory(Message<?> failedMessage) {
        String processingPath = failedMessage.getHeaders().get(PROCESSING_FILE_HEADER, String.class);
        File payloadFile = failedMessage.getPayload() instanceof File file ? file : null;

        Path source = null;
        if (processingPath != null) {
            source = Path.of(processingPath);
        } else if (payloadFile != null) {
            source = payloadFile.toPath();
        }

        if (source == null || !Files.exists(source)) {
            log.warn("No file to move to error directory for message {}", failedMessage);
            return;
        }

        try {
            Path target = Path.of(properties.getErrorDirectory()).resolve(source.getFileName());
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
            log.error("Moved failed file to {}", target);
        } catch (IOException ex) {
            log.error("Failed to move file {} to error directory", source, ex);
        }
    }

    private String toXmlFileName(String processingFileName) {
        String name = processingFileName;
        String suffix = properties.getProcessingSuffix();
        if (name.endsWith(suffix)) {
            name = name.substring(0, name.length() - suffix.length());
        }
        int dot = name.lastIndexOf('.');
        String base = dot > 0 ? name.substring(0, dot) : name;
        return base + ".xml";
    }
}
