package com.seglad.csvconverter.config;

import com.seglad.csvconverter.properties.ConverterProperties;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DirectorySetupConfig {

    private final ConverterProperties properties;

    public DirectorySetupConfig(ConverterProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    void ensureDirectoriesExist() throws IOException {
        Files.createDirectories(Path.of(properties.getInputDirectory()));
        Files.createDirectories(Path.of(properties.getOutputDirectory()));
        Files.createDirectories(Path.of(properties.getErrorDirectory()));
    }
}
