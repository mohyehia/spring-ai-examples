package com.moh.yehia.mcp;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class ChatController {
    private final ChatClient chatClient;

    public ChatController(ChatClient.Builder chatClient, ToolCallbackProvider toolCallbackProvider) {
        this.chatClient = chatClient
                .defaultToolCallbacks(toolCallbackProvider)
                .build();
    }

//    @GetMapping
//    public String chat(@RequestParam String question) {
//        return chatClient.prompt()
//                .system("You are a useful assistant")
//                .toolContext(Map.of("progressToken", "token-" + new Random().nextInt()))
//                .user(question)
//                .advisors(new SimpleLoggerAdvisor())
//                .call()
//                .content();
//    }

    @GetMapping
    public String chat() {
        String userPrompt = """
                Check the weather forecast in Berlin right now and show the creative response!
                you will need first to get the location coordinates (latitude & longitude) for the city in order to know exactly the correct weather forecast.
                """;
        return chatClient.prompt()
                .toolContext(Map.of("progressToken", "token-" + System.currentTimeMillis()))
                .system("You are a useful assistant")
                .user(userPrompt)
                .advisors(new SimpleLoggerAdvisor())
                .call()
                .content();
    }
}
