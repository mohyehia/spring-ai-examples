package com.moh.yehia.evaluator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Stream;

@Component
public class IngestionService implements ApplicationRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(IngestionService.class);
    private final VectorStore vectorStore;

    @Value("classpath:documents.txt")
    private Resource resource;

    public IngestionService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        LOGGER.info("Starting ingestion of documents from resource: {}", resource.getFilename());
        String content = new String(resource.getInputStream().readAllBytes());
        List<String> lines = Stream.of(content.split("\\n"))
                .filter(line -> !line.isBlank())
                .toList();
        lines.forEach(line -> {
            Document document = Document.builder()
                    .id(String.valueOf(line.hashCode()))
                    .text(line)
                    .build();
            vectorStore.add(List.of(document));
        });
        LOGGER.info("Completed ingestion of {} documents into the vector store", lines.size());
    }
}
