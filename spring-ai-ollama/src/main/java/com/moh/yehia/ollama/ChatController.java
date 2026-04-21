package com.moh.yehia.ollama;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.SafeGuardAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
public class ChatController {
    private final ChatClient chatClient;
    private static final Logger LOGGER = LoggerFactory.getLogger(ChatController.class);

    public ChatController(ChatClient.Builder builder) {
        this.chatClient = builder
                .defaultSystem("You are a helpful assistant that provides concise and short answers")
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .defaultAdvisors(new SafeGuardAdvisor(List.of("inappropriate", "harmful", "words")))
                .defaultAdvisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, UUID.randomUUID().toString()))
//                .defaultOptions(ChatOptions.builder()
//                        .temperature(0.5)
//                        .build())
                .build();
    }

    @GetMapping
    public String getJoke() {
        ChatClientResponse chatClientResponse = chatClient.prompt()
                .user("Tell me a joke about computers")
                .call()
                .chatClientResponse();
        LOGGER.info("Received response from model: {}", chatClientResponse);
        return chatClientResponse.toString();
    }

    @GetMapping("/ask")
    public String chat(@RequestParam String question) {
        return chatClient
                .prompt()
                .user(question)
                .call()
                .content();
    }
}
