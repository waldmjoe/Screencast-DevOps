package com.example.chat_backend;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import jakarta.annotation.PostConstruct;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;


record ChatRequest(String prompt) {}

record OllamaRequest(String model, String prompt, String system, boolean stream, List<Long> context) {}

record OllamaResponse(
    String model,
    String created_at,
    String response,
    boolean done,
    String done_reason,
    List<Long> context,
    long total_duration,
    long load_duration,
    int prompt_eval_count,
    long prompt_eval_duration,
    int eval_count,
    long eval_duration
) {}

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final WebClient webClient;
    private final String ollamaModel;
    private String systemPrompt;

    public ChatController(WebClient.Builder webClientBuilder,
                          @Value("${ollama.api.url}") String ollamaApiUrl,
                          @Value("${ollama.model}") String ollamaModel) {
        this.webClient = webClientBuilder.baseUrl(ollamaApiUrl).build();
        this.ollamaModel = ollamaModel;
    }

    @PostConstruct
    public void loadSystemPrompt() {
        try {
            ClassPathResource resource = new ClassPathResource("system_prompt.txt");
            if (resource.exists()) {
                InputStreamReader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8);
                this.systemPrompt = FileCopyUtils.copyToString(reader);
                System.out.println("Successfully loaded system prompt: " + this.systemPrompt.substring(0, Math.min(this.systemPrompt.length(), 100)) + "..."); // Log first 100 chars
            } else {
                System.out.println("system_prompt.txt not found, using default or no system prompt.");
                this.systemPrompt = null; 
            }
        } catch (IOException e) {
            System.err.println("Error loading system_prompt.txt: " + e.getMessage());
            this.systemPrompt = null; 
        }
    }

    @PostMapping
    public Mono<OllamaResponse> chat(@RequestBody ChatRequest request) {
        OllamaRequest ollamaRequest = new OllamaRequest(
            this.ollamaModel,
            request.prompt(),
            this.systemPrompt, 
            false,
            null
        );

        return webClient.post()
                .uri("/api/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(ollamaRequest)
                .retrieve()
                .bodyToMono(OllamaResponse.class)
                .map(response -> {
                    System.out.println("Ollama raw response: " + response.response());
                    return response;
                });
    }
}