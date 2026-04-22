package com.moh.yehia.chat.memory;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.redis.RedisChatMemoryRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/memory")
public class RedisChatMemoryController {
    private final ChatClient chatClient;
    private final RedisChatMemoryRepository redisChatMemoryRepository;

    public RedisChatMemoryController(ChatClient.Builder chatClient, RedisChatMemoryRepository redisChatMemoryRepository) {
        this.chatClient = chatClient
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .build();
        this.redisChatMemoryRepository = redisChatMemoryRepository;
    }

    @GetMapping("/{username}/chat")
    public String chat(@PathVariable String username, @RequestParam String question) {
        return chatClient.prompt()
                .user(question)
                .advisors(PromptChatMemoryAdvisor.builder(MessageWindowChatMemory.builder()
                        .chatMemoryRepository(redisChatMemoryRepository)
                        .build()).build())
                .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, username))
                .call()
                .content();
    }
}
