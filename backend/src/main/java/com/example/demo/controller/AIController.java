package com.example.demo.controller;

import com.example.demo.service.OpenAIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "*")
public class AIController {

    @Autowired
    private OpenAIService openAIService;

    @PostMapping("/reply")
    public ResponseEntity<Map<String, String>> generateReply(@RequestBody Map<String, String> request) {
        Map<String, String> response = new HashMap<>();
        
        try {
            String comment = request.get("comment");
            if (comment == null || comment.trim().isEmpty()) {
                response.put("error", "댓글 내용이 없습니다.");
                return ResponseEntity.badRequest().body(response);
            }

            String reply = openAIService.generateReply(comment);
            response.put("reply", reply);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("error", "답변 생성 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/answer")
    public ResponseEntity<Map<String, String>> generateAnswer(@RequestBody Map<String, Object> request) {
        Map<String, String> response = new HashMap<>();
        
        try {
            String content = (String) request.get("content");
            if (content == null || content.trim().isEmpty()) {
                response.put("error", "내용이 없습니다.");
                return ResponseEntity.badRequest().body(response);
            }

            String answer = openAIService.generateAnswer(content);
            response.put("answer", answer);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("error", "답변 생성 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "ok");
        response.put("message", "AI 서비스가 정상적으로 작동 중입니다.");
        return ResponseEntity.ok(response);
    }
}

