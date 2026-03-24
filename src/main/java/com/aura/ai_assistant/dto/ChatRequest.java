package com.aura.ai_assistant.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {
    private String message;
    private String conversationId;  // Optional: to continue existing conversation
    private String sessionId;        // Optional: for frontend session tracking
    private String promptType;       // "coding", "creative", "general"
    private Long userId;
}