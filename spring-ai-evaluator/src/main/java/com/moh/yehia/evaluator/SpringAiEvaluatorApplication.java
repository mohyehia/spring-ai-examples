package com.moh.yehia.evaluator;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SpringAiEvaluatorApplication {

    static void main(String[] args) {
        SpringApplication.run(SpringAiEvaluatorApplication.class, args);
    }

    @Bean
    SimpleVectorStore simpleVectorStore(EmbeddingModel embeddingModel) {
        return SimpleVectorStore.builder(embeddingModel).build();
    }

//    @Bean
//    public ObservationRegistry observationRegistry() {
//        return ObservationRegistry.create();
//    }

}
