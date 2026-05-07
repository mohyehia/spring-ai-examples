package com.moh.yehia.mcp;

import io.modelcontextprotocol.spec.McpSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.mcp.annotation.McpLogging;
import org.springframework.stereotype.Component;

@Component
public class McpLoggerHandler {
    private static final Logger log = LoggerFactory.getLogger(McpLoggerHandler.class);

    @McpLogging(clients = "weather-service")
    void loggingHandler(McpSchema.LoggingMessageNotification loggingMessageNotification) {
        switch (loggingMessageNotification.level()) {
            case INFO ->
                    log.info("Received log: {} - {}", loggingMessageNotification.level(), loggingMessageNotification.data());
            case WARNING ->
                    log.warn("Received log: {} - {}", loggingMessageNotification.level(), loggingMessageNotification.data());
            case ERROR ->
                    log.error("Received log: {} - {}", loggingMessageNotification.level(), loggingMessageNotification.data());
            default ->
                    log.debug("Received log: {} - {}", loggingMessageNotification.level(), loggingMessageNotification.data());
        }
    }

}
