package com.moh.yehia.chat.memory;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ChatController {
    private final ChatClient chatClient;
    private final InMemoryChatMemoryRepository inMemoryChatMemoryRepository;

    public ChatController(ChatClient.Builder chatClient) {
        this.chatClient = chatClient
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .build();
        this.inMemoryChatMemoryRepository = new InMemoryChatMemoryRepository();
    }

    @GetMapping("/chat")
    public String ask(@RequestParam String question) {
        return chatClient.prompt()
                .user(question)
                .advisors(PromptChatMemoryAdvisor.builder(MessageWindowChatMemory.builder().chatMemoryRepository(inMemoryChatMemoryRepository).build()).build())
                .call()
                .content();
    }
}
