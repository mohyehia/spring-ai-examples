package com.moh.yehia.rag;

import io.micrometer.observation.ObservationRegistry;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping
public class ChatController {
    private final VectorStore vectorStore;

    public ChatController(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    @GetMapping
    public List<Document> chat() {
        List<Document> documents = List.of(
                new Document("Spring AI rocks!! Spring AI rocks!! Spring AI rocks!! Spring AI rocks!! Spring AI rocks!!", Map.of("meta1", "meta1")),
                new Document("The World is Big and Salvation Lurks Around the Corner"),
                new Document("You walk forward facing the past and you turn back toward the future.", Map.of("meta2", "meta2")));
        vectorStore.accept(documents);
        return vectorStore.similaritySearch(SearchRequest.builder()
                .topK(2)
                .query("spring")
                .build());
    }

    @Bean
    public ObservationRegistry observationRegistry() {
        return ObservationRegistry.create();
    }
}
