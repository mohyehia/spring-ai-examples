package com.moh.yehia.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.ToolCallAdvisor;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.ai.tool.augment.AugmentedToolCallbackProvider;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping
public class ChatController {
    private final ChatClient chatClient;
    private final ToolCallingManager toolCallingManager;
    private static final Logger log = LoggerFactory.getLogger(ChatController.class);

    public ChatController(ChatClient.Builder chatClient, ToolCallingManager toolCallingManager) {
        this.chatClient = chatClient.build();
        this.toolCallingManager = toolCallingManager;
    }

    @GetMapping
    public String chat(@RequestParam String question) {
        /*
        *   As an alternative to the framework-controlled tool execution, you can use the ToolCallAdvisor to implement tool calling as part of the advisor chain. This approach provides several advantages:
            Observability: Other advisors in the chain can intercept and observe each tool call iteration
            Integration with Chat Memory: Works seamlessly with Chat Memory advisors for conversation history management
            Extensibility: The advisor can be extended to customize the tool calling behavior
        * */
        var toolCallAdvisor = ToolCallAdvisor.builder()
//                .disableInternalConversationHistory() // This is useful only when integrating with a Chat Memory advisor that already manages conversation history
                .toolCallingManager(toolCallingManager)
                .build();

        return chatClient.prompt()
                .advisors(new SimpleLoggerAdvisor(), toolCallAdvisor)
                .tools(new DateTimeTools(), new WeatherTools(), new CustomerTools())
                .user(question)
                .call()
                .content();
    }

    @GetMapping("/{customerNumber}")
    String getCustomerInfo(@PathVariable String customerNumber) {
        var toolCallAdvisor = ToolCallAdvisor.builder()
                .toolCallingManager(toolCallingManager)
                .build();
        /*
         * Tool Argument Augmentation
         * Spring AI provides a utility for dynamic augmentation of tool input schemas with additional arguments.
         * This allows capturing extra information from the model—such as reasoning or metadata—without modifying the underlying tool implementation.
         * The reasoning will be retrieved if the model supports the reasoning capabilities
         * */
        AugmentedToolCallbackProvider<AgentThinking> toolCallbackProvider = AugmentedToolCallbackProvider.<AgentThinking>builder()
                .toolObject(new CustomerTools())
                .argumentType(AgentThinking.class)
                .argumentConsumer(event -> {
                    // You can implement any custom logic here based on the agent's thinking before calling the tool, for example:
                    AgentThinking agentThinking = event.arguments();
                    log.info("Tool: {} | Reasoning: {}", event.toolDefinition().name(), agentThinking.innerThought());
                })
                .removeExtraArgumentsAfterProcessing(true)
                .build();
        return chatClient.prompt()
                .user("Retrieve the customer information for the customer number: %s".formatted(customerNumber))
                .advisors(new SimpleLoggerAdvisor(), toolCallAdvisor)
                .toolCallbacks(toolCallbackProvider)
                .call()
                .content();
    }
}

record AgentThinking(
        @ToolParam(description = "Your reasoning for calling this tool")
        String innerThought,
        @ToolParam(description = "Confidence level (low, medium, high)", required = false)
        String confidence
) {

}

class DateTimeTools {
    @Tool(description = "Get the current date and time in the user's timezone")
    String getCurrentDateTime() {
        System.out.println("Retrieving the current date time for the user");
        return LocalDateTime.now().atZone(LocaleContextHolder.getTimeZone().toZoneId()).toString();
    }

    @Tool(description = "Set a user alarm for the given time")
    String setAlarm(@ToolParam(description = "The number of minutes to set an alarm after") String minutes) {
        System.out.println("Setting an alarm for the user after: " + minutes);
        String time = LocalDateTime.now().plusMinutes(Long.parseLong(minutes)).atZone(LocaleContextHolder.getTimeZone().toZoneId()).toString();
        return "Alarm set for %s".formatted(time);
    }
}

class WeatherTools {

    @Tool(description = "Get the current weather information based on the given country")
    String getCurrentWeather(@ToolParam(description = "country or city to retrieve the weather info for") String countryOrCity) {
        System.out.println("Retrieving the weather information for the country: " + countryOrCity);
        return "The current temperature now at %s is %s degree!".formatted(countryOrCity, Math.random() * 40);
    }
}

class CustomerTools {

    private final Map<String, CustomerInfo> data = new HashMap<>();

    CustomerTools() {
        data.put("123", new CustomerInfo("123", "John Doe", "123 Main St"));
        data.put("456", new CustomerInfo("456", "Jane Smith", "456 Elm St"));
        data.put("789", new CustomerInfo("789", "Bob Johnson", "789 Oak St"));
    }

    @Tool(description = "Retrieve the customer information based on the given customer number", returnDirect = true)
    CustomerInfo findByCustomerNumber(@ToolParam(description = "Provided customer number") String customerNumber) {
        System.out.println("Retrieving the customer information for the customer number: " + customerNumber);
        return data.getOrDefault(customerNumber, new CustomerInfo(customerNumber, "Unknown", "Unknown"));
    }
}

record CustomerInfo(String customerNumber, String customerName, String customerAddress) {
}