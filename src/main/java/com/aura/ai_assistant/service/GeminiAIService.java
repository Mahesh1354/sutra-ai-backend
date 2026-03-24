package com.aura.ai_assistant.service;

import com.aura.ai_assistant.config.GeminiConfig;
import com.aura.ai_assistant.dto.ChatRequest;
import com.aura.ai_assistant.dto.ChatResponse;
import com.aura.ai_assistant.model.Conversation;
import com.aura.ai_assistant.model.Message;
import com.aura.ai_assistant.model.User;
import com.aura.ai_assistant.repository.ConversationRepository;
import com.aura.ai_assistant.repository.MessageRepository;
import com.aura.ai_assistant.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GeminiAIService {

    @Autowired
    private GeminiConfig geminiConfig;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    public ChatResponse processMessage(ChatRequest request) {
        try {
            // 1. Get the user
            User user = null;
            if (request.getUserId() != null) {
                user = userRepository.findById(request.getUserId())
                        .orElseThrow(() -> new RuntimeException("User not found"));
            } else {
                // For backward compatibility, create or get default user
                user = getOrCreateDefaultUser();
            }

            // 2. Get or create conversation
            Conversation conversation = getOrCreateConversation(request, user);

            // 3. Save user message
            saveUserMessage(conversation, request.getMessage());

            // 4. Build prompt with context
            String prompt = buildPromptWithContext(request, conversation);

            // 5. Call Gemini API
            String aiResponse = callGeminiAPI(prompt);

            // 6. Save assistant message
            Message assistantMessage = saveAssistantMessage(conversation, aiResponse);

            // 7. Generate suggestions (optional)
            List<String> suggestions = generateSuggestions(aiResponse);

            // 8. Build response
            return ChatResponse.builder()
                    .id(UUID.randomUUID().toString())
                    .message(aiResponse)
                    .conversationId(conversation.getId().toString())
                    .timestamp(LocalDateTime.now())
                    .suggestions(suggestions)
                    .model(geminiConfig.getModel())
                    .build();

        } catch (Exception e) {
            e.printStackTrace();
            return ChatResponse.builder()
                    .id(UUID.randomUUID().toString())
                    .message("I'm having trouble connecting to my AI service. Please try again later.")
                    .conversationId(request.getConversationId())
                    .timestamp(LocalDateTime.now())
                    .build();
        }
    }

    private User getOrCreateDefaultUser() {
        // For backward compatibility, find or create a default user
        return userRepository.findByUsername("default")
                .orElseGet(() -> {
                    User defaultUser = new User();
                    defaultUser.setUsername("default");
                    defaultUser.setEmail("default@example.com");
                    defaultUser.setPassword("default");
                    defaultUser.setFullName("Default User");
                    return userRepository.save(defaultUser);
                });
    }

    private Conversation getOrCreateConversation(ChatRequest request, User user) {
        if (request.getConversationId() != null) {
            Optional<Conversation> existing = conversationRepository.findById(Long.parseLong(request.getConversationId()));
            if (existing.isPresent()) {
                // Verify the conversation belongs to this user
                if (existing.get().getUser() != null &&
                        !existing.get().getUser().getId().equals(user.getId())) {
                    throw new RuntimeException("Access denied to this conversation");
                }
                return existing.get();
            }
        }

        // Create new conversation
        Conversation newConversation = new Conversation();
        newConversation.setTitle(generateTitleFromMessage(request.getMessage()));
        newConversation.setSessionId(request.getSessionId() != null ?
                request.getSessionId() :
                UUID.randomUUID().toString().substring(0, 8));
        newConversation.setUser(user); // Set the user relationship

        return conversationRepository.save(newConversation);
    }

    private String generateTitleFromMessage(String message) {
        if (message.length() > 30) {
            return message.substring(0, 27) + "...";
        }
        return message;
    }

    private void saveUserMessage(Conversation conversation, String content) {
        Message message = new Message();
        message.setType(Message.MessageType.USER);
        message.setContent(content);
        message.setConversation(conversation);
        messageRepository.save(message);
    }

    private Message saveAssistantMessage(Conversation conversation, String content) {
        Message message = new Message();
        message.setType(Message.MessageType.ASSISTANT);
        message.setContent(content);
        message.setConversation(conversation);
        return messageRepository.save(message);
    }

    private String buildPromptWithContext(ChatRequest request, Conversation conversation) {
        StringBuilder promptBuilder = new StringBuilder();

        // Add system prompt based on type
        switch (request.getPromptType() != null ? request.getPromptType() : "general") {
            case "coding":
                promptBuilder.append("You are an expert programming assistant. Help with code, debugging, and best practices. ");
                promptBuilder.append("Provide code examples when relevant. Use markdown for code blocks.\n\n");
                break;
            case "creative":
                promptBuilder.append("You are a creative writing assistant. Help with stories, poems, and creative ideas. ");
                promptBuilder.append("Be imaginative and inspiring.\n\n");
                break;
            default:
                promptBuilder.append("You are Aura, a helpful AI assistant. Be friendly, accurate, and concise. ");
                promptBuilder.append("You can help with coding, writing, and general questions.\n\n");
        }

        // Add recent conversation history for context (last 5 messages)
        List<Message> recentMessages = messageRepository.findTop10ByConversationIdOrderByCreatedAtDesc(conversation.getId())
                .stream()
                .limit(5)
                .collect(Collectors.toList());

        if (!recentMessages.isEmpty()) {
            promptBuilder.append("Recent conversation:\n");
            Collections.reverse(recentMessages); // Show in chronological order
            for (Message msg : recentMessages) {
                promptBuilder.append(msg.getType()).append(": ").append(msg.getContent()).append("\n");
            }
            promptBuilder.append("\n");
        }

        // Add current user message
        promptBuilder.append("User: ").append(request.getMessage()).append("\n");
        promptBuilder.append("Assistant: ");

        return promptBuilder.toString();
    }

    private String callGeminiAPI(String prompt) {
        try {
            String url = geminiConfig.getFullApiUrl() + "?key=" + geminiConfig.getApiKey();

            System.out.println("Calling Gemini API URL: " + url); // For debugging

            // Build request body
            Map<String, Object> requestBody = new HashMap<>();

            // Content parts
            Map<String, String> textPart = new HashMap<>();
            textPart.put("text", prompt);

            List<Map<String, String>> parts = new ArrayList<>();
            parts.add(textPart);

            Map<String, Object> content = new HashMap<>();
            content.put("parts", parts);

            List<Map<String, Object>> contents = new ArrayList<>();
            contents.add(content);

            requestBody.put("contents", contents);

            // Generation config
            Map<String, Object> generationConfig = new HashMap<>();
            generationConfig.put("temperature", geminiConfig.getTemperature());
            generationConfig.put("maxOutputTokens", geminiConfig.getMaxTokens());
            generationConfig.put("topP", 0.95);
            generationConfig.put("topK", 40);

            requestBody.put("generationConfig", generationConfig);

            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // Make API call
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            System.out.println("Gemini Response: " + response.getBody()); // For debugging

            // Parse response
            return parseGeminiResponse(response.getBody());

        } catch (Exception e) {
            e.printStackTrace();
            return "I encountered an error while processing your request: " + e.getMessage();
        }
    }

    private String parseGeminiResponse(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);

            // Navigate to the text in Gemini's response structure
            JsonNode candidates = root.path("candidates");
            if (candidates.isArray() && candidates.size() > 0) {
                JsonNode content = candidates.get(0).path("content");
                JsonNode parts = content.path("parts");
                if (parts.isArray() && parts.size() > 0) {
                    return parts.get(0).path("text").asText();
                }
            }

            return "I received an unexpected response format from Gemini.";

        } catch (Exception e) {
            e.printStackTrace();
            return "Error parsing Gemini response: " + e.getMessage();
        }
    }

    private List<String> generateSuggestions(String aiResponse) {
        // Simple suggestion generation based on response content
        List<String> suggestions = new ArrayList<>();

        if (aiResponse.toLowerCase().contains("code") || aiResponse.contains("```")) {
            suggestions.add("Can you explain this code?");
            suggestions.add("Show me another example");
            suggestions.add("How can I optimize this?");
        } else if (aiResponse.toLowerCase().contains("story") || aiResponse.toLowerCase().contains("write")) {
            suggestions.add("Continue this story");
            suggestions.add("Write a different version");
            suggestions.add("Add more characters");
        } else {
            suggestions.add("Tell me more");
            suggestions.add("Give me an example");
            suggestions.add("Explain in simpler terms");
        }

        return suggestions;
    }
}