package com.moh.yehia.chat.memory;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.ai.chat.messages.Message;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping
public class ChatMemoryController {
    private final ChatClient chatClient;
    private final JdbcChatMemoryRepository jdbcChatMemoryRepository;

    public ChatMemoryController(ChatClient.Builder chatClient, JdbcChatMemoryRepository jdbcChatMemoryRepository) {
        this.chatClient = chatClient.build();
        this.jdbcChatMemoryRepository = jdbcChatMemoryRepository;
    }

    @GetMapping("/{username}")
    public String ask(@PathVariable String username, @RequestParam String question) {

        return chatClient.prompt()
                .advisors(new SimpleLoggerAdvisor())
                .advisors(PromptChatMemoryAdvisor.builder(
                        MessageWindowChatMemory.builder()
                                .chatMemoryRepository(jdbcChatMemoryRepository)
                                .build()
                ).build())
                .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, username))
                .user(question)
                .call()
                .content();
    }

    @GetMapping("/{username}/conversations")
    public List<String> viewConversations(@PathVariable String username) {
        return jdbcChatMemoryRepository.findByConversationId(username)
                .stream().map(Message::getText).toList();
    }

    @GetMapping("/conversations")
    public List<String> viewConversationIDs() {
        return jdbcChatMemoryRepository.findConversationIds();
    }
}
