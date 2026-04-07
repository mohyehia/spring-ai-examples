package com.moh.yehia.ollama;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/metadata")
public class MetadataController {
    private final ChatClient chatClient;
    private final ChatClient defaultChatClient;
    private final Logger log = LoggerFactory.getLogger(MetadataController.class);

    public MetadataController(ChatClient.Builder builder) {
        this.chatClient = builder.build();
        this.defaultChatClient = builder
                .defaultSystem(promptSystemSpec -> promptSystemSpec.text("You are a helpful assistant")
                        .metadata("assistantType", "general")
                        .metadata("version", "2.0")
                ).defaultUser(promptUserSpec -> promptUserSpec.text("Default user context")
                        .metadata("sessionId", "default-session-id")
                )
                .build();
    }

    @GetMapping
    public ChatResponse testMetadata() {
        ChatResponse chatResponse = chatClient.prompt()
                .system(promptSystemSpec -> promptSystemSpec.text("You are a helpful assistant")
                        .metadata("version", "1.0")
                )
                .user(promptUserSpec -> promptUserSpec.text("Tell me a joke about programming")
                        .metadata("messageId", UUID.randomUUID().toString())
                        .metadata("userId", "logged-in-userId")
                        .metadata("priority", "high")
                ).call()
                .chatResponse();
        log.info("ChatResponse =>{}", chatResponse);
        return chatResponse;
    }

    @GetMapping("/default")
    public ChatResponse testDefaultChatClient() {
        return defaultChatClient.prompt("Tell me a joke about computers")
                .advisors(new SimpleLoggerAdvisor())
                .call()
                .chatResponse();
    }

    @GetMapping("/custom-logger-advisor")
    public String testCustomLoggerAdvisor() {
        SimpleLoggerAdvisor customLoggerAdvisor = new SimpleLoggerAdvisor(
                request -> {
                    assert request != null;
                    return "Custom request: " + request.prompt();
                },
                response -> {
                    assert response != null;
                    return "Custom response: " + response.getResult();
                },
                0
        );
        return chatClient.prompt("Tell me a joke about computers")
                .advisors(customLoggerAdvisor)
                .call()
                .content();
    }

}