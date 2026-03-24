package com.aura.ai_assistant.controller;

import com.aura.ai_assistant.dto.ChatRequest;
import com.aura.ai_assistant.dto.ChatResponse;
import com.aura.ai_assistant.model.Conversation;
import com.aura.ai_assistant.model.Message;
import com.aura.ai_assistant.model.User;
import com.aura.ai_assistant.repository.ConversationRepository;
import com.aura.ai_assistant.repository.MessageRepository;
import com.aura.ai_assistant.service.GeminiAIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class ChatController {

    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);

    @Autowired
    private GeminiAIService geminiAIService;

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private MessageRepository messageRepository;

    @GetMapping("/conversations")
    public ResponseEntity<?> getConversations(Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
            }

            User user = (User) authentication.getPrincipal();
            logger.info("Fetching conversations for user: {}", user.getId());

            List<Conversation> conversations = conversationRepository.findByUserIdOrderByUpdatedAtDesc(user.getId());

            List<Map<String, Object>> result = conversations.stream().map(conv -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", conv.getId());
                map.put("title", conv.getTitle());
                map.put("createdAt", conv.getCreatedAt());
                map.put("updatedAt", conv.getUpdatedAt());
                try {
                    map.put("messageCount", messageRepository.countByConversationId(conv.getId()));
                } catch (Exception e) {
                    map.put("messageCount", 0);
                }
                return map;
            }).collect(Collectors.toList());

            logger.info("Found {} conversations", result.size());
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            logger.error("Error fetching conversations: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to fetch conversations: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PostMapping("/send")
    public ResponseEntity<?> sendMessage(@RequestBody ChatRequest request, Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
            }

            User user = (User) authentication.getPrincipal();
            logger.info("User {} sending message", user.getId());

            request.setUserId(user.getId()); // Add userId to ChatRequest DTO
            ChatResponse response = geminiAIService.processMessage(request);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error sending message: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to send message: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<?> getConversationMessages(@PathVariable Long conversationId, Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
            }

            User user = (User) authentication.getPrincipal();
            logger.info("User {} fetching messages for conversation: {}", user.getId(), conversationId);

            // Verify conversation belongs to user
            Conversation conversation = conversationRepository.findById(conversationId)
                    .orElseThrow(() -> new RuntimeException("Conversation not found"));

            if (!conversation.getUser().getId().equals(user.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied to this conversation");
            }

            List<Message> messages = messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);

            List<Map<String, Object>> result = messages.stream().map(msg -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", msg.getId());
                map.put("content", msg.getContent());
                map.put("type", msg.getType());
                map.put("createdAt", msg.getCreatedAt());
                map.put("metadata", msg.getMetadata());
                return map;
            }).collect(Collectors.toList());

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            logger.error("Error fetching messages: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to fetch messages: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @DeleteMapping("/conversations/{conversationId}")
    public ResponseEntity<?> deleteConversation(@PathVariable Long conversationId, Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
            }

            User user = (User) authentication.getPrincipal();
            logger.info("User {} deleting conversation: {}", user.getId(), conversationId);

            // Verify conversation belongs to user
            Conversation conversation = conversationRepository.findById(conversationId)
                    .orElseThrow(() -> new RuntimeException("Conversation not found"));

            if (!conversation.getUser().getId().equals(user.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied to this conversation");
            }

            conversationRepository.deleteById(conversationId);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Conversation deleted successfully");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error deleting conversation: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to delete conversation: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}