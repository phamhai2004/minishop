package com.example.minishop.repository;

import com.example.minishop.entity.ChatConversation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatConversationRepository
        extends JpaRepository<ChatConversation, Long> {

    Optional<ChatConversation>
    findByCustomer_IdAndShop_Id(
            Long customerId,
            Long shopId
    );

    List<ChatConversation>
    findByCustomer_IdOrderByUpdatedAtDesc(
            Long customerId
    );

    List<ChatConversation>
    findByShop_Owner_IdOrderByUpdatedAtDesc(
            Long sellerId
    );
}