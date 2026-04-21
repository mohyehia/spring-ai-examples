package com.moh.yehia.ollama;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/multi-modal")
public class MultiModalController {
    private final ChatClient chatClient;

    public MultiModalController(ChatClient.Builder chatClient) {
        this.chatClient = chatClient.build();
    }

    @GetMapping
    public String imageInquiry() {
        return chatClient
                .prompt()
                .advisors(new SimpleLoggerAdvisor())
                .system("You are a helpful assistant that can analyze images and provide insights.")
                .user(promptUserSpec -> promptUserSpec.text("Analyze the image and describe its content in detail.")
                        .media(MimeTypeUtils.IMAGE_PNG, new ClassPathResource("/images/multimodal.test.png")))
                .call()
                .content();
    }
}
