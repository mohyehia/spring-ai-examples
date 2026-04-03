package com.moh.yehia.ollama;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RequestMapping("/prompts")
@RestController
public class PromptController {
    private static final Logger LOGGER = LoggerFactory.getLogger(PromptController.class);
    private final ChatClient chatClient;

    @Value("classpath:prompts/youtube-prompt.st")
    private Resource toutubePromptResource;

    @Value("classpath:prompts/system-prompt.st")
    private Resource systemPromptResource;

    public PromptController(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @GetMapping
    public String simple() {
        ChatResponse chatResponse = chatClient.prompt(new Prompt("What is the capital of France?"))
                .call()
                .chatResponse();
        assert chatResponse != null;
        LOGGER.info("Received response from model: {}", chatResponse);
        LOGGER.info("Received Results from model: {}", chatResponse.getResults());
        LOGGER.info("Received metadata from model: {}", chatResponse.getMetadata());
        chatResponse.getResults().forEach(result -> LOGGER.info("Result content from text: {}", result.getOutput().getText()));
        return chatResponse.getResults().getFirst().getOutput().getText();
    }

    @GetMapping("/popular")
    public String popular(@RequestParam(defaultValue = "tech") String genre) {
        Prompt prompt = new PromptTemplate(toutubePromptResource)
                .create(Map.of("genre", genre));
        ChatResponse chatResponse = chatClient.prompt(prompt)
                .call()
                .chatResponse();
        assert chatResponse != null;
        return chatResponse.getResults().getFirst().getOutput().getText();
    }

    @GetMapping("/instruct")
    public String instructModel(@RequestParam(defaultValue = "Tell me a Joke") String userInput) {
        SystemMessage systemMessage = new SystemMessage(systemPromptResource);
        UserMessage userMessage = new UserMessage(userInput);
        Prompt prompt = new Prompt(systemMessage, userMessage);
        ChatResponse chatResponse = chatClient.prompt(prompt)
                .call()
                .chatResponse();
        assert chatResponse != null;
        LOGGER.info("Received chatResponse from model: {}", chatResponse);
        return chatResponse.getResults().getFirst().getOutput().getText();
    }
}
