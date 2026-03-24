package com.aura.ai_assistant.controller;

import com.aura.ai_assistant.model.Conversation;
import com.aura.ai_assistant.model.Message;
import com.aura.ai_assistant.model.User;
import com.aura.ai_assistant.repository.ConversationRepository;
import com.aura.ai_assistant.repository.MessageRepository;
import com.aura.ai_assistant.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/create-test-conversation")
    public String createTestConversation() {
        // Get or create test user
        User testUser = getOrCreateTestUser();

        // Create a new conversation
        Conversation conversation = new Conversation();
        conversation.setTitle("Test Conversation");
        conversation.setSessionId(UUID.randomUUID().toString().substring(0, 8));
        conversation.setUser(testUser);  // Set the user object instead of userId

        // Save conversation
        conversation = conversationRepository.save(conversation);

        // Create a user message
        Message userMessage = new Message();
        userMessage.setType(Message.MessageType.USER);
        userMessage.setContent("Hello, this is a test message!");
        userMessage.setConversation(conversation);

        // Create an assistant message
        Message assistantMessage = new Message();
        assistantMessage.setType(Message.MessageType.ASSISTANT);
        assistantMessage.setContent("Hi! This is a test response from the assistant.");
        assistantMessage.setConversation(conversation);

        // Save messages
        messageRepository.save(userMessage);
        messageRepository.save(assistantMessage);

        return "Test conversation created with ID: " + conversation.getId() + " for user: " + testUser.getUsername();
    }

    @GetMapping("/conversations")
    public List<Conversation> getAllConversations() {
        // Get or create test user
        User testUser = getOrCreateTestUser();
        // Find conversations by user ID
        return conversationRepository.findByUserIdOrderByUpdatedAtDesc(testUser.getId());
    }

    @GetMapping("/conversations/{userId}")
    public List<Conversation> getUserConversations(@PathVariable Long userId) {
        return conversationRepository.findByUserIdOrderByUpdatedAtDesc(userId);
    }

    @PostMapping("/create-user")
    public String createTestUser() {
        User testUser = getOrCreateTestUser();
        return "Test user created/retrieved with ID: " + testUser.getId() +
                ", Username: " + testUser.getUsername();
    }

    private User getOrCreateTestUser() {
        // Try to find existing test user
        return userRepository.findByUsername("test-user")
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setUsername("test-user");
                    newUser.setEmail("test@example.com");
                    newUser.setPassword("password"); // This will be encoded in production
                    newUser.setFullName("Test User");
                    return userRepository.save(newUser);
                });
    }
}