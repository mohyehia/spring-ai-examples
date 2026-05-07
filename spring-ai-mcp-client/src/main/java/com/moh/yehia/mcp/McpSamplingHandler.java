package com.moh.yehia.mcp;

import io.modelcontextprotocol.spec.McpSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.mcp.annotation.McpSampling;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class McpSamplingHandler {
    private static final Logger log = LoggerFactory.getLogger(McpSamplingHandler.class);

    private final ChatClient chatClient;

    public McpSamplingHandler(@Lazy ChatClient.Builder chatClient) {
        this.chatClient = chatClient.build();
    }

    @McpSampling(clients = "weather-service")
    public McpSchema.CreateMessageResult samplingHandler(McpSchema.CreateMessageRequest createMessageRequest) {

        log.info("MCP SAMPLING: {}", createMessageRequest);

        List<Message> messages = createMessageRequest.messages().stream()
                .map(message -> {
                    if (message.role() == McpSchema.Role.USER)
                        return new UserMessage(((McpSchema.TextContent) message.content()).text());
                    else return AssistantMessage.builder()
                            .content(((McpSchema.TextContent) message.content()).text())
                            .build();
                }).collect(Collectors.toUnmodifiableList());

        String content = chatClient
                .prompt(new Prompt(messages))
                .system(createMessageRequest.systemPrompt())
                .user(((McpSchema.TextContent) createMessageRequest.messages().getFirst().content()).text())
                .call()
                .content();

        return McpSchema.CreateMessageResult.builder()
                .role(McpSchema.Role.ASSISTANT)
                .model(createMessageRequest.modelPreferences().hints().getFirst().name())
                .content(new McpSchema.TextContent(content)).build();
    }
}
