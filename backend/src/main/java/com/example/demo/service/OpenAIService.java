package com.example.demo.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OpenAIService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${openai.api.key:}")
    private String apiKey;

    @Value("${openai.model:gpt-3.5-turbo}")
    private String model;

    public OpenAIService() {
        this.webClient = WebClient.builder()
                .baseUrl("https://api.openai.com/v1")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public String generateReply(String comment) {
        if (apiKey == null || apiKey.isEmpty()) {
            throw new RuntimeException("OpenAI API 키가 설정되지 않았습니다. application.properties에 openai.api.key를 설정해주세요.");
        }

        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            
            List<Map<String, String>> messages = List.of(
                    Map.of("role", "system", 
                           "content", "당신은 친절하고 전문적인 고객 서비스 담당자입니다. 사용자의 댓글에 대해 도움이 되는 답변을 한국어로 작성합니다."),
                    Map.of("role", "user", 
                           "content", "다음 댓글에 대해 친절하고 도움이 되는 답변을 한국어로 작성해주세요:\n\n" + comment)
            );
            requestBody.put("messages", messages);
            requestBody.put("temperature", 0.7);
            requestBody.put("max_tokens", 500);

            String response = webClient.post()
                    .uri("/chat/completions")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .body(BodyInserters.fromValue(requestBody))
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(30))
                    .block();

            JsonNode jsonNode = objectMapper.readTree(response);
            String reply = jsonNode.path("choices")
                    .get(0)
                    .path("message")
                    .path("content")
                    .asText();

            return reply != null && !reply.isEmpty() ? reply : "답변을 생성할 수 없습니다.";
        } catch (Exception e) {
            throw new RuntimeException("OpenAI API 호출 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    public String generateAnswer(String content) {
        if (apiKey == null || apiKey.isEmpty()) {
            throw new RuntimeException("OpenAI API 키가 설정되지 않았습니다. application.properties에 openai.api.key를 설정해주세요.");
        }

        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            
            List<Map<String, String>> messages = List.of(
                    Map.of("role", "system", 
                           "content", "당신은 전문적인 콘텐츠 작성자입니다. 주어진 내용에 대해 적절하고 도움이 되는 답변을 한국어로 작성합니다."),
                    Map.of("role", "user", 
                           "content", "다음 내용에 대해 적절한 답변을 한국어로 작성해주세요:\n\n" + content)
            );
            requestBody.put("messages", messages);
            requestBody.put("temperature", 0.7);
            requestBody.put("max_tokens", 1000);

            String response = webClient.post()
                    .uri("/chat/completions")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .body(BodyInserters.fromValue(requestBody))
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(30))
                    .block();

            JsonNode jsonNode = objectMapper.readTree(response);
            String answer = jsonNode.path("choices")
                    .get(0)
                    .path("message")
                    .path("content")
                    .asText();

            return answer != null && !answer.isEmpty() ? answer : "답변을 생성할 수 없습니다.";
        } catch (Exception e) {
            throw new RuntimeException("OpenAI API 호출 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }
}

