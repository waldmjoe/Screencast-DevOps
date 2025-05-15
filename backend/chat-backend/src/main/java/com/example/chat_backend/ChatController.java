package com.example.chat_backend;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;


record ChatRequest(String prompt) {}

record OllamaRequest(String model, String prompt, boolean stream, List<Long> context) {}

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

    public ChatController(WebClient.Builder webClientBuilder,
                          @Value("${ollama.api.url}") String ollamaApiUrl,
                          @Value("${ollama.model}") String ollamaModel) {
        this.webClient = webClientBuilder.baseUrl(ollamaApiUrl).build();
        this.ollamaModel = ollamaModel;
    }

    @PostMapping
    public Mono<OllamaResponse> chat(@RequestBody ChatRequest request) {
        // For stateless chat, pass null or empty list for context
        OllamaRequest ollamaRequest = new OllamaRequest(this.ollamaModel, request.prompt(), false, null);

        return webClient.post()
                .uri("/api/generate") // Ollama's generation endpoint
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
