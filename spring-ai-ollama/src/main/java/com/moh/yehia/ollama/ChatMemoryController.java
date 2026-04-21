package com.moh.yehia.ollama;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/memory")
public class ChatMemoryController {
    private final Logger log = LoggerFactory.getLogger(ChatMemoryController.class);
    private final ChatClient chatClient;

    private final InMemoryChatMemoryRepository inMemoryChatMemoryRepository;

    private final Map<String, MessageChatMemoryAdvisor> messageChatMemoryAdvisorMap = new ConcurrentHashMap<>();

    private final Map<String, PromptChatMemoryAdvisor> promptChatMemoryAdvisorMap = new ConcurrentHashMap<>();

    public ChatMemoryController(ChatClient.Builder chatClient) {
        this.chatClient = chatClient
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .build();
        this.inMemoryChatMemoryRepository = new InMemoryChatMemoryRepository();
    }

    @GetMapping
    String ask(@RequestParam String question) {
        MessageWindowChatMemory messageWindowChatMemory = MessageWindowChatMemory.builder()
                .maxMessages(5)
                .build();
        return chatClient.prompt()
                .user(question)
                .advisors(MessageChatMemoryAdvisor.builder(messageWindowChatMemory).build())
                .call()
                .content();
    }

    @GetMapping("/{user}/ask")
    String askPerUser(@PathVariable String user, @RequestParam String question) {
        MessageWindowChatMemory messageWindowChatMemory = MessageWindowChatMemory.builder().chatMemoryRepository(inMemoryChatMemoryRepository).build();
//        MessageChatMemoryAdvisor messageChatMemoryAdvisor = MessageChatMemoryAdvisor.builder(messageWindowChatMemory).build();
//        var advisorPerUser = messageChatMemoryAdvisorMap.computeIfAbsent(user, k -> messageChatMemoryAdvisor);

        PromptChatMemoryAdvisor promptChatMemoryAdvisor = PromptChatMemoryAdvisor.builder(messageWindowChatMemory).build();

        return chatClient.prompt()
                .user(question)
                .options(OllamaChatOptions.builder()
                        .disableThinking()
                        .build())
                .advisors(promptChatMemoryAdvisor)
                .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, user))
                .call()
                .content();
    }

    @GetMapping("/conversations-ids")
    List<String> findConversationIds() {
        List<String> conversationIds = inMemoryChatMemoryRepository.findConversationIds();
        conversationIds.forEach(id -> {
            List<Message> messages = inMemoryChatMemoryRepository.findByConversationId(id);
            messages.forEach(message -> log.info("Conversation ID: {}, Role: {}, Content: {}", id, message.getMessageType(), message.getText()));
        });
        return conversationIds;
    }

    @GetMapping("/conversations/{id}")
    List<Message> findConversations(@PathVariable String id) {
        return inMemoryChatMemoryRepository.findByConversationId(id);
    }
}
