package com.aura.ai_assistant.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {
    private String id;
    private String message;
    private String conversationId;
    private LocalDateTime timestamp;
    private List<String> suggestions;  // Optional: suggested follow-up questions
    private Integer tokensUsed;        // Optional: token count
    private String model;              // Which model was used
}