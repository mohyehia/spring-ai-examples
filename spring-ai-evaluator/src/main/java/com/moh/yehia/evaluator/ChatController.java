package com.moh.yehia.evaluator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.evaluation.FactCheckingEvaluator;
import org.springframework.ai.chat.evaluation.RelevancyEvaluator;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.evaluation.EvaluationRequest;
import org.springframework.ai.evaluation.EvaluationResponse;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Objects;

@RestController
public class ChatController {
    private final ChatClient chatClient;
    private final VectorStore vectorStore;
    private final ChatModel chatModel;

    private static final Logger LOGGER = LoggerFactory.getLogger(ChatController.class);

    public ChatController(ChatClient.Builder chatClient, VectorStore vectorStore, ChatModel chatModel) {
        this.chatClient = chatClient.build();
        this.vectorStore = vectorStore;
        this.chatModel = chatModel;
    }

    @GetMapping
    public String chat(@RequestParam(defaultValue = "What is Spring AI?") String question) {
//        RetrievalAugmentationAdvisor retrievalAugmentationAdvisor = RetrievalAugmentationAdvisor.builder()
//                .documentRetriever(VectorStoreDocumentRetriever.builder()
//                        .vectorStore(vectorStore)
//                        .build())
//                .build();

        return chatClient.prompt()
                .user(question)
                .advisors(new SimpleLoggerAdvisor())
                .advisors(QuestionAnswerAdvisor.builder(vectorStore).build())
                .call()
                .content();
    }


    @GetMapping("/evaluate")
    public EvaluationResponse evaluate(@RequestParam(defaultValue = "What is Spring AI?") String question) {
        RetrievalAugmentationAdvisor retrievalAugmentationAdvisor = RetrievalAugmentationAdvisor.builder()
                .documentRetriever(VectorStoreDocumentRetriever.builder()
                        .vectorStore(vectorStore)
                        .build())
                .build();

        ChatResponse chatResponse = chatClient.prompt()
                .user(question)
                .advisors(new SimpleLoggerAdvisor())
                .advisors(retrievalAugmentationAdvisor)
                .call()
                .chatResponse();

        LOGGER.info("Start evaluating model response!");
        // perform the evaluation
        EvaluationRequest evaluationRequest = new EvaluationRequest(
                // the original user question
                question,
                // The retrieved context from the RAG flow
                Objects.requireNonNull(chatResponse.getMetadata().get(RetrievalAugmentationAdvisor.DOCUMENT_CONTEXT)),
                // The AI model response
                Objects.requireNonNull(chatResponse.getResult().getOutput().getText()));

        LOGGER.info("evaluationRequest: {}", evaluationRequest);
        /*
         * The RelevancyEvaluator is an implementation of the Evaluator interface, designed to assess the relevance of AI-generated responses against provided context.
         *  This evaluator helps assess the quality of a RAG flow by determining if the AI model’s response is relevant to the user’s input with respect to the retrieved context.
         * */
        RelevancyEvaluator relevancyEvaluator = new RelevancyEvaluator(ChatClient.builder(chatModel));
        EvaluationResponse evaluationResponse = relevancyEvaluator.evaluate(evaluationRequest);
        LOGGER.info("Evaluation completed: {}", evaluationResponse);
        return evaluationResponse;
    }

    @GetMapping("/fact-check")
    public String factCheck(@RequestParam(defaultValue = "The Earth is the fourth planet from the Sun") String claim) {

        var factCheckingEvaluator = FactCheckingEvaluator.builder(ChatClient.builder(chatModel)).build();
        // Example context and claim
        String context = "The Earth is the third planet from the Sun and the only astronomical object known to harbor life.";

        // Create an EvaluationRequest
        EvaluationRequest evaluationRequest = new EvaluationRequest(context, Collections.emptyList(), claim);
        // Perform the evaluation
        EvaluationResponse evaluationResponse = factCheckingEvaluator.evaluate(evaluationRequest);
        return evaluationResponse.toString();
    }
}
