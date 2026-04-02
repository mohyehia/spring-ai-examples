package com.moh.yehia.ollama;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ChatController {
    private final ChatClient chatClient;
    private static final Logger LOGGER = LoggerFactory.getLogger(ChatController.class);

    public ChatController(ChatClient.Builder builder) {
        this.chatClient = builder.build();
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
}
