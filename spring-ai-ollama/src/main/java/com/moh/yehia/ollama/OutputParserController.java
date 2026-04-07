package com.moh.yehia.ollama;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ResponseEntity;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.converter.ListOutputConverter;
import org.springframework.ai.converter.MapOutputConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/parser")
public class OutputParserController {
    private final ChatClient chatClient;

    @Value("classpath:prompts/song-text-prompt.st")
    private Resource textPrompt;

    @Value("classpath:prompts/song-structured-prompt.st")
    private Resource structuredPrompt;

    public OutputParserController(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @GetMapping
    public String getSongs(@RequestParam(defaultValue = "Adele") String artist) {
        Prompt prompt = new PromptTemplate(textPrompt)
                .create(Map.of("artist", artist));
        ChatResponse chatResponse = chatClient.prompt(prompt)
                .call()
                .chatResponse();
        assert chatResponse != null;
        return chatResponse.getResults()
                .getFirst()
                .getOutput()
                .getText();
    }

    @GetMapping("/list")
    public List<String> getSongsAsList(@RequestParam(defaultValue = "Adele") String artist) {
        ListOutputConverter listOutputConverter = new ListOutputConverter();
        Prompt prompt = new PromptTemplate(structuredPrompt)
                .create(Map.of("artist", artist, "format", listOutputConverter.getFormat()));
        String content = chatClient.prompt(prompt)
                .call()
                .content();
        assert content != null;
        return listOutputConverter.convert(content);
    }

    @GetMapping("/map")
    public Map<String, Object> getSongsAsMap(@RequestParam(defaultValue = "Adele") String artist) {
        MapOutputConverter mapOutputConverter = new MapOutputConverter();

        Prompt prompt = new PromptTemplate(structuredPrompt)
                .create(Map.of("artist", artist, "format", mapOutputConverter.getFormat()));
        String content = chatClient.prompt(prompt)
                .call()
                .content();
        assert content != null;
        return mapOutputConverter.convert(content);
    }

    @GetMapping("/dto")
    public List<SongDTO> getSongsStructured(@RequestParam(defaultValue = "Adele") String artist) {
        BeanOutputConverter<List<SongDTO>> beanOutputConverter = new BeanOutputConverter<>(new ParameterizedTypeReference<>() {
        });

        Prompt prompt = new PromptTemplate(structuredPrompt)
                .create(Map.of("artist", artist, "format", beanOutputConverter.getFormat()));
        String content = chatClient.prompt(prompt)
                .call()
                .content();
        assert content != null;
        return beanOutputConverter.convert(content);
    }

    @GetMapping("/entity")
    public List<SongDTO> getNativeSongs(@RequestParam(defaultValue = "Adele") String artist) {
        return chatClient.prompt(new PromptTemplate(textPrompt)
                        .create(Map.of("artist", artist)))
                .call()
                .entity(new ParameterizedTypeReference<>() {
                });
    }

    @GetMapping("/response-entity")
    public ResponseEntity<ChatResponse, List<SongDTO>> getResponseEntitySongs(@RequestParam(defaultValue = "Adele") String artist) {
        return chatClient.prompt(new PromptTemplate(textPrompt)
                        .create(Map.of("artist", artist)))
                .call()
                .responseEntity(new ParameterizedTypeReference<>() {
                });
    }

    @GetMapping("/stream")
    public Flux<String> getSongsAsStream(@RequestParam(defaultValue = "Adele") String artist) {
        Prompt prompt = new PromptTemplate(textPrompt)
                .create(Map.of("artist", artist));
        return chatClient.prompt(prompt)
                .stream()
                .content();
    }
}