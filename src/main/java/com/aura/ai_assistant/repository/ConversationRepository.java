package com.aura.ai_assistant.repository;

import com.aura.ai_assistant.model.Conversation;
import com.aura.ai_assistant.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    // This should be findByUser_Id, not findByUserId
    List<Conversation> findByUser_IdOrderByUpdatedAtDesc(Long userId);

    // Alternative: If you want to keep the same method name, add this:
    List<Conversation> findByUserIdOrderByUpdatedAtDesc(Long userId);

    // You can also find by user object
    List<Conversation> findByUserOrderByUpdatedAtDesc(User user);
}