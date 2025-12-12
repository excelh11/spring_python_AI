package com.example.demo.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.nio.file.Paths;

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

        /**
     * Python 전처리 스크립트 호출
     * - preprocess.py 에게 content를 넘기고
     * - JSON 결과에서 cleaned_text 를 꺼내서 반환
     */
    private String preprocessWithPython(String content) {
    try {
        System.out.println("====== [PY] 전처리 시작 ======");
        System.out.println("[PY] 입력 content: " + content);

        // 현재 작업 디렉터리 확인용 로그
        System.out.println("[PY] 현재 작업 디렉터리 = " + Paths.get("").toAbsolutePath());

        // 1) python + preprocess.py + content 로 프로세스 실행
        ProcessBuilder pb = new ProcessBuilder(
                "python", "preprocess.py", content
        );

        // 2) backend 기준으로 ../python-preprocessing 폴더로 이동
        //    (backend 와 python-preprocessing 이 같은 선상에 있으니까)
        pb.directory(new File("../python-preprocessing"));

        System.out.println("[PY] 실행 디렉터리: " + pb.directory().getAbsolutePath());
        System.out.println("[PY] 디렉터리 존재 여부: " 
                + pb.directory().getAbsolutePath() + " -> " + pb.directory().exists());
        System.out.println("[PY] 명령어: " + String.join(" ", pb.command()));

        // stderr 도 stdout 으로 합치기 (에러 메시지도 같이 받기)
        pb.redirectErrorStream(true);

        Process process = pb.start();

        // 3) Python stdout 읽기 (UTF-8)
        byte[] bytes = process.getInputStream().readAllBytes();
        int exitCode = process.waitFor();

        String output = new String(bytes, StandardCharsets.UTF_8);

        System.out.println("[PY] 프로세스 종료 코드(exitCode): " + exitCode);
        System.out.println("[PY] raw output (stdout+stderr):");
        System.out.println(output);

        if (exitCode != 0) {
            System.out.println("[PY] 전처리 실패. 예외를 발생시킵니다.");
            throw new RuntimeException("Python 전처리 실패 (exitCode=" + exitCode + "): " + output);
        }

        // 4) JSON 파싱
        JsonNode root = objectMapper.readTree(output);

        String cleanedText = root.path("cleaned_text").asText();
        System.out.println("[PY] 파싱된 cleaned_text: " + cleanedText);

        System.out.println("====== [PY] 전처리 종료 ======");

        // cleaned_text 가 비어 있으면 원본 content 로 fallback
        return (cleanedText != null && !cleanedText.isEmpty()) ? cleanedText : content;

    } catch (Exception e) {
        System.out.println("[PY] 전처리 중 예외 발생: " + e.getMessage());
        e.printStackTrace();
        throw new RuntimeException("Python 전처리 호출 중 오류가 발생했습니다: " + e.getMessage(), e);
    }
}


    public String generateReply(String comment) {
        if (apiKey == null || apiKey.isEmpty()) {
            throw new RuntimeException("OpenAI API 키가 설정되지 않았습니다. application.properties에 openai.api.key를 설정해주세요.");
        }

        try {
            //  1) 먼저 Python 전처리 수행
            String preprocessedComment = preprocessWithPython(comment);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);

            //  2) OpenAI 에는 전처리된 텍스트를 기반으로 보냄
            List<Map<String, String>> messages = List.of(
                    Map.of("role", "system",
                           "content", "당신은 친절하고 전문적인 고객 서비스 담당자입니다. 사용자의 댓글에 대해 도움이 되는 답변을 한국어로 작성합니다."),
                    Map.of("role", "user",
                           "content",
                                   "다음 댓글에 대해 친절하고 도움이 되는 답변을 한국어로 작성해주세요.\n\n"
                                           + "원본 댓글:\n" + comment + "\n\n"
                                           + "전처리된 댓글:\n" + preprocessedComment)
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
            //  1) 먼저 Python 전처리 수행
            String preprocessedContent = preprocessWithPython(content);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);

            List<Map<String, String>> messages = List.of(
                    Map.of("role", "system",
                           "content", "당신은 전문적인 콘텐츠 작성자입니다. 주어진 내용에 대해 적절하고 도움이 되는 답변을 한국어로 작성합니다."),
                    Map.of("role", "user",
                           "content",
                                   "다음 내용에 대해 적절한 답변을 한국어로 작성해주세요.\n\n"
                                           + "원본 내용:\n" + content + "\n\n"
                                           + "전처리된 내용:\n" + preprocessedContent)
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
