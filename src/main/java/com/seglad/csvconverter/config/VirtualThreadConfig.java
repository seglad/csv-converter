package com.seglad.csvconverter.config;

import java.util.concurrent.Executors;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.core.task.support.TaskExecutorAdapter;

@Configuration
public class VirtualThreadConfig {

    public static final String CSV_CONVERTER_TASK_EXECUTOR = "csvConverterTaskExecutor";

    @Bean(name = CSV_CONVERTER_TASK_EXECUTOR)
    public TaskExecutor csvConverterTaskExecutor() {
        return new TaskExecutorAdapter(Executors.newVirtualThreadPerTaskExecutor());
    }
}
