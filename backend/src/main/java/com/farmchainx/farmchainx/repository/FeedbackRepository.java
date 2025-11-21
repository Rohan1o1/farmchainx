package com.farmchainx.farmchainx.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.farmchainx.farmchainx.model.Feedback;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    List<Feedback> findByProductId(Long productId);

    Optional<Feedback> findByProductIdAndConsumerId(Long productId, Long consumerId);

    boolean existsByProductIdAndConsumerId(Long productId, Long consumerId);
}
