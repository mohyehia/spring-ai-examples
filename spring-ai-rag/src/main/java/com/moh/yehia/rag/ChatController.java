package com.moh.yehia.rag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

@RestController
@RequestMapping
public class ChatController {
    private static final Logger log = LoggerFactory.getLogger(ChatController.class);
    private final ChatClient chatClient;
    private final SimpleVectorStore vectorStore;
    private final EmbeddingModel embeddingModel;

    @Value("classpath:documents.txt")
    private Resource resource;

    public ChatController(ChatClient.Builder chatClient, SimpleVectorStore vectorStore, EmbeddingModel embeddingModel) {
        this.chatClient = chatClient.build();
        this.vectorStore = vectorStore;
        this.embeddingModel = embeddingModel;
    }

    @GetMapping
    public String chat(@RequestParam String question) {
        QuestionAnswerAdvisor questionAnswerAdvisor = QuestionAnswerAdvisor.builder(vectorStore)
                .searchRequest(SearchRequest.builder()
                        .query(question)
                        .similarityThreshold(0.8)
                        .topK(6)
                        .build())
                .build();
        return chatClient.prompt()
                .user(question)
                .advisors(new SimpleLoggerAdvisor(), questionAnswerAdvisor)
                .call()
                .content();
    }

    @GetMapping("/embedding")
    public EmbeddingResponse embed(@RequestParam(defaultValue = "Tell me a joke") String message) {
        EmbeddingResponse embeddingResponse = embeddingModel.embedForResponse(List.of(message));
        log.info("Retrieved embedding for message '{}': {}", message, embeddingResponse);
        int dimensions = embeddingModel.dimensions();
        log.info("dimensions: {}", dimensions);
        return embeddingResponse;
    }

    @GetMapping("/load")
    public String load() throws IOException {
        String content = new String(resource.getInputStream().readAllBytes());
        List<String> lines = Stream.of(content.split("\\n"))
                .filter(line -> !line.isBlank())
                .toList();
        lines.forEach(line -> log.info("logging {}", line));
        log.info("Loaded {} lines from the resource file", lines.size());
        lines.forEach(line -> {
            Document document = Document.builder()
                    .id(String.valueOf(line.hashCode()))
                    .text(line)
                    .build();
            vectorStore.add(List.of(document));
        });
        return "Loaded data into the vectorStore named %s".formatted(vectorStore.getName());
    }

    @GetMapping("/vector-search")
    List<Document> search(@RequestParam String question) {
        List<Document> documents = vectorStore.doSimilaritySearch(SearchRequest.builder()
                .query(question)
                .similarityThreshold(0.8)
                .topK(6)
                .build());
        documents.forEach(document -> log.info("Retrieved document with id '{}', text '{}', and metadata '{}'", document.getId(), document.getText(), document.getMetadata()));
        return documents;
    }

}
