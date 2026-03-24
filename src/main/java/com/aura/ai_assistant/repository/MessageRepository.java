package com.aura.ai_assistant.repository;

import com.aura.ai_assistant.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    // Find all messages in a conversation
    List<Message> findByConversationIdOrderByCreatedAtAsc(Long conversationId);

    // Get last N messages from a conversation
    List<Message> findTop10ByConversationIdOrderByCreatedAtDesc(Long conversationId);

    // Count messages in a conversation
    long countByConversationId(Long conversationId);

    // Search messages by content (for future features)
    @Query("SELECT m FROM Message m WHERE m.conversation.id = :conversationId AND LOWER(m.content) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Message> searchMessages(@Param("conversationId") Long conversationId, @Param("keyword") String keyword);
}