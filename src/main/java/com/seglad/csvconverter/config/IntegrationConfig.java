package com.seglad.csvconverter.config;

import com.seglad.csvconverter.integration.FilePipelineService;
import com.seglad.csvconverter.properties.ConverterProperties;
import java.io.File;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.file.FileReadingMessageSource;
import org.springframework.integration.file.FileWritingMessageHandler;
import org.springframework.integration.file.filters.AcceptOnceFileListFilter;
import org.springframework.integration.file.filters.CompositeFileListFilter;
import org.springframework.integration.file.filters.RegexPatternFileListFilter;
import org.springframework.integration.file.support.FileExistsMode;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.support.ErrorMessage;
import org.springframework.messaging.support.MessageBuilder;

@Configuration
public class IntegrationConfig {

    private final ConverterProperties properties;
    private final FilePipelineService pipelineService;
    private final TaskExecutor csvConverterTaskExecutor;

    public IntegrationConfig(
            ConverterProperties properties,
            FilePipelineService pipelineService,
            @Qualifier(VirtualThreadConfig.CSV_CONVERTER_TASK_EXECUTOR) TaskExecutor csvConverterTaskExecutor) {
        this.properties = properties;
        this.pipelineService = pipelineService;
        this.csvConverterTaskExecutor = csvConverterTaskExecutor;
    }

    @Bean
    public FileReadingMessageSource csvFileSource() {
        FileReadingMessageSource source = new FileReadingMessageSource();
        source.setDirectory(new File(properties.getInputDirectory()));
        source.setAutoCreateDirectory(true);
        CompositeFileListFilter<File> filter = new CompositeFileListFilter<>();
        filter.addFilter(new AcceptOnceFileListFilter<>());
        filter.addFilter(new RegexPatternFileListFilter("(?i).*\\.csv$"));
        source.setFilter(filter);
        return source;
    }

    @Bean
    public FileWritingMessageHandler xmlFileWriter() {
        FileWritingMessageHandler handler =
                new FileWritingMessageHandler(new File(properties.getOutputDirectory()));
        handler.setAutoCreateDirectory(true);
        handler.setExpectReply(false);
        handler.setFileExistsMode(FileExistsMode.REPLACE);
        return handler;
    }

    @Bean
    public IntegrationFlow fileConversionFlow(
            FileReadingMessageSource csvFileSource, FileWritingMessageHandler xmlFileWriter) {
        return IntegrationFlow.from(csvFileSource, spec -> spec.poller(Pollers.fixedDelay(properties.getPollIntervalMs())
                        .maxMessagesPerPoll(properties.getMaxMessagesPerPoll())
                        .taskExecutor(csvConverterTaskExecutor)))
                .transform(File.class, this::claimSafely)
                .transform(File.class, this::buildXmlMessageSafely)
                .handle(xmlFileWriter)
                .handle((payload, headers) -> {
                    pipelineService.deleteProcessingFile(
                            MessageBuilder.createMessage(payload, headers));
                    return null;
                })
                .get();
    }

    @Bean
    public IntegrationFlow errorHandlingFlow() {
        return IntegrationFlow.from("errorChannel")
                .handle((payload, headers) -> {
                    Message<?> failed = payload instanceof ErrorMessage errorMessage
                            ? errorMessage.getOriginalMessage()
                            : payload instanceof Message<?> message
                                    ? message
                                    : MessageBuilder.createMessage(payload, headers);
                    pipelineService.moveToErrorDirectory(failed);
                    return null;
                })
                .get();
    }

    private File claimSafely(File csvFile) {
        try {
            return pipelineService.claim(csvFile);
        } catch (IOException ex) {
            throw new MessagingException("Failed to claim file " + csvFile, ex);
        }
    }

    private Message<byte[]> buildXmlMessageSafely(File processingFile) {
        try {
            return pipelineService.buildXmlMessage(processingFile);
        } catch (IOException ex) {
            throw new MessagingException("Failed to convert file " + processingFile, ex);
        }
    }
}
