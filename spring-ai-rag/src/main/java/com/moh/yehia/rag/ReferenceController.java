package com.moh.yehia.rag;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.model.transformer.KeywordMetadataEnricher;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@RestController
@RequestMapping("/spring")
public class ReferenceController {

    private final ChatClient chatClient;
    private final SimpleVectorStore simpleVectorStore;
    private final ChatModel chatModel;

    @Value("classpath:documents.txt")
    private Resource resource;


    public ReferenceController(ChatClient.Builder chatClient, SimpleVectorStore simpleVectorStore, ChatModel chatModel) {
        this.chatClient = chatClient
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .build();
        this.simpleVectorStore = simpleVectorStore;
        this.chatModel = chatModel;
    }

    @GetMapping
    public String chat(@RequestParam(defaultValue = "What is Spring?") String question) {
        return chatClient.prompt()
                .advisors(QuestionAnswerAdvisor.builder(simpleVectorStore).build())
                .user(question)
                .call()
                .content();
    }

    @GetMapping("/vector-search")
    public List<Document> vectorSearch(@RequestParam String question) {
        return simpleVectorStore.doSimilaritySearch(SearchRequest.builder()
                .query(question)
                .similarityThreshold(0.4)
                .topK(5)
                .build());
    }

    @GetMapping("/enrich")
    public String enrich() throws IOException {
        String content = new String(resource.getInputStream().readAllBytes());
        List<String> lines = Stream.of(content.split("\\n"))
                .filter(line -> !line.isBlank())
                .toList();
        List<Document> documents = new ArrayList<>();
        lines.forEach(line -> {
            Document document = Document.builder()
                    .id(String.valueOf(line.hashCode()))
                    .text(line)
                    .build();
            documents.add(document);
        });

        KeywordMetadataEnricher keywordMetadataEnricher = KeywordMetadataEnricher.builder(chatModel)
                .keywordCount(5)
                .build();

        List<Document> applied = keywordMetadataEnricher.apply(documents);
        applied.forEach(document -> System.out.printf("Document ID: %s, Text: %s, Keywords: %s%n", document.getId(), document.getText(), document.getMetadata().get("excerpt_keywords")));
        return "documents applied successfully!";
    }

}
