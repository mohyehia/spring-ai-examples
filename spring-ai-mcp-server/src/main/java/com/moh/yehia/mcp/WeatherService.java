package com.moh.yehia.mcp;

import io.modelcontextprotocol.spec.McpSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.mcp.annotation.McpProgressToken;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.ai.mcp.annotation.context.McpSyncRequestContext;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;

@Service
public class WeatherService {
    private static final Logger log = LoggerFactory.getLogger(WeatherService.class);

    record WeatherResponse(Current current) {
        record Current(LocalDateTime time, int interval, double temperature_2m) {
        }
    }

    record LocationResponse(double latitude, double longitude) {
    }

//    @McpTool(description = "Get the temperature (in celsius) for a specific location")
//    public String getTemperature(
//            McpSyncServerExchange mcpSyncServerExchange,
//            @McpToolParam(description = "The location latitude") double latitude,
//            @McpToolParam(description = "The location longitude") double longitude

    /// /            @McpProgressToken String progressToken
//    ) throws InterruptedException {
//        log.info("Calling the MCP Tool");
//        mcpSyncServerExchange.loggingNotification(McpSchema.LoggingMessageNotification.builder()
//                .level(McpSchema.LoggingLevel.DEBUG)
//                .data("Fetching weather data for latitude: " + latitude + ", longitude: " + longitude)
//                .meta(Map.of())
//                .build());
//        WeatherResponse weatherResponse = RestClient.create()
//                .get()
//                .uri("https://api.open-meteo.com/v1/forecast?latitude={latitude}&longitude={longitude}&current=temperature_2m", latitude, longitude)
//                .retrieve()
//                .body(WeatherResponse.class);
//        String epicPoem = "MCP Client doesn't provide sampling capability.";
//        if (mcpSyncServerExchange.getClientCapabilities().sampling() != null) {
//            log.info("MCP Client supports sampling. Simulating a long-running operation with progress notifications.");
//            mcpSyncServerExchange.progressNotification(new McpSchema.ProgressNotification("123456798", 0.5, 1.0, "Start Sampling"));
//            Thread.sleep(5000);
//            String samplingMessage = """
//                    For a weather forecast (temperature is in Celsius): %s.
//                    At location with latitude: %s and longitude: %s.
//                    Please write an epic poem about this forecast using a Shakespearean style.
//                    """.formatted(weatherResponse.current().temperature_2m(), latitude, longitude);
//            McpSchema.CreateMessageResult samplingResponse = mcpSyncServerExchange.createMessage(McpSchema.CreateMessageRequest.builder()
//                    .systemPrompt("You are a poet!")
//                    .messages(List.of(new McpSchema.SamplingMessage(McpSchema.Role.USER, new McpSchema.TextContent(samplingMessage))))
//                    .build()); // (5)
//            epicPoem = ((McpSchema.TextContent) samplingResponse.content()).text();
//        }
//        mcpSyncServerExchange.progressNotification(new McpSchema.ProgressNotification("123456798", 1.0, 1.0, "Finished Sampling"));
//        return """
//                Weather Poem: %s
//                about the weather: %s°C at location: (%s, %s)
//                """.formatted(epicPoem, weatherResponse.current().temperature_2m(), latitude, longitude);
//    }
    @McpTool(description = "Get the location coordinates (latitude & longitude) for a specific address")
    public LocationResponse getLocationCoordinates(
            McpSyncRequestContext mcpSyncRequestContext,
            @McpToolParam(description = "The location or city") String location,
            @McpProgressToken String progressToken) {
        mcpSyncRequestContext.info("Fetching location coordinates data for location: %s".formatted(location));
        mcpSyncRequestContext.progress(new McpSchema.ProgressNotification(progressToken, 50, 100.0, "Start fetching coordinates data for received location"));

//        mcpSyncRequestContext.progress(p -> p.progress(50).total(100).message("Start fetching coordinates data"));

        log.info("Received progress token => {} from the client", progressToken);

        return switch (location) {
            case "Amsterdam" -> {
                mcpSyncRequestContext.info("Finished fetching location coordinates data for location: %s".formatted(location));
                mcpSyncRequestContext.progress(new McpSchema.ProgressNotification(progressToken, 100, 100.0, "Finished fetching coordinates data for received location"));
//                mcpSyncRequestContext.progress(p -> p.progress(100).total(100).message("Finished fetching coordinates data"));
//                mcpSyncRequestContext.ping();
                yield new LocationResponse(52.3676, 4.9041);
            }
            case "Paris" -> {
                mcpSyncRequestContext.info("Finished fetching location coordinates data for location: %s".formatted(location));
                mcpSyncRequestContext.progress(new McpSchema.ProgressNotification(progressToken, 100, 100.0, "Finished fetching coordinates data for received location"));
//                mcpSyncRequestContext.progress(p -> p.progress(100).total(100).message("Finished fetching coordinates data"));
//                mcpSyncRequestContext.ping();
                yield new LocationResponse(48.8534951, 2.3483915);
            }
            case "Berlin" -> {
                mcpSyncRequestContext.info("Finished fetching location coordinates data for location: %s".formatted(location));
                mcpSyncRequestContext.progress(new McpSchema.ProgressNotification(progressToken, 100, 100.0, "Finished fetching coordinates data for received location"));
//                mcpSyncRequestContext.progress(p -> p.progress(100).total(100).message("Finished fetching coordinates data"));
//                mcpSyncRequestContext.ping();
                yield new LocationResponse(52.520008, 13.404954);
            }
            default -> new LocationResponse(30.044420, 31.235712);
        };
    }

    @McpTool(description = "Get the weather forecast for a specific coordinates (latitude & longitude)")
    public WeatherResponse getWeatherForecast(
            McpSyncRequestContext mcpSyncRequestContext,
            @McpToolParam(description = "The location latitude") double latitude,
            @McpToolParam(description = "The location longitude") double longitude) {
        mcpSyncRequestContext.debug("Fetching weather data for latitude: %s, longitude: %s".formatted(latitude, longitude));

        String tokenFromMetadata = mcpSyncRequestContext.requestMeta().getOrDefault("progressToken", "no progress token!").toString();
        log.info("token from metadata => {}", tokenFromMetadata);

        String progressToken = mcpSyncRequestContext.request().progressToken().toString();
        log.info("progressToken from context =>{}", progressToken);

        mcpSyncRequestContext.progress(p -> p.progress(50).total(100).message("Start fetching weather data for the provided coordinates"));
        WeatherResponse weatherResponse = RestClient.create()
                .get()
                .uri("https://api.open-meteo.com/v1/forecast?latitude={latitude}&longitude={longitude}&current=temperature_2m", latitude, longitude)
                .retrieve()
                .body(WeatherResponse.class);
        assert weatherResponse != null;
        mcpSyncRequestContext.debug("Finished fetching weather data: %s°C".formatted(weatherResponse.current().temperature_2m()));
        mcpSyncRequestContext.progress(p -> p.progress(100).total(100).message("Finished fetching weather data"));
        mcpSyncRequestContext.ping();
        return weatherResponse;
    }
}
