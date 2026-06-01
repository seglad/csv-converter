package com.seglad.csvconverter;

import com.seglad.csvconverter.properties.ConverterProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.integration.config.EnableIntegration;

@SpringBootApplication
@EnableIntegration
@EnableConfigurationProperties(ConverterProperties.class)
public class CsvConverterApplication {

    public static void main(String[] args) {
        SpringApplication.run(CsvConverterApplication.class, args);
    }
}
