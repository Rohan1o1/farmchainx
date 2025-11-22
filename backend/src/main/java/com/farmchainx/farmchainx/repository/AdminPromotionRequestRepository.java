package com.farmchainx.farmchainx.repository;

import com.farmchainx.farmchainx.model.AdminPromotionRequest;
import com.farmchainx.farmchainx.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AdminPromotionRequestRepository extends JpaRepository<AdminPromotionRequest, Long> {

    List<AdminPromotionRequest> findByApprovedFalseAndRejectedFalse();

    boolean existsByUserAndApprovedFalseAndRejectedFalse(User user);

    AdminPromotionRequest findByUserAndApprovedFalseAndRejectedFalse(User user);
}