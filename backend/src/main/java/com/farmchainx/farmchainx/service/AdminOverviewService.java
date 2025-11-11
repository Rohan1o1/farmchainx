package com.farmchainx.farmchainx.service;

import org.springframework.stereotype.Service;

import com.farmchainx.farmchainx.dto.AdminOverview;
import com.farmchainx.farmchainx.repository.FeedbackRepository;
import com.farmchainx.farmchainx.repository.ProductRepository;
import com.farmchainx.farmchainx.repository.SupplyChainLogRepository;
import com.farmchainx.farmchainx.repository.UserRepository;

@Service
public class AdminOverviewService {

	private final UserRepository userRepo;
    private final ProductRepository productRepo;
    private final SupplyChainLogRepository supplyChainLogRepo;
    private final FeedbackRepository feedbackRepo;

    public AdminOverviewService(UserRepository userRepo,
                                ProductRepository productRepo,
                                SupplyChainLogRepository supplyChainLogRepo,
                                FeedbackRepository feedbackRepo) {
        this.userRepo = userRepo;
        this.productRepo = productRepo;
        this.supplyChainLogRepo = supplyChainLogRepo;
        this.feedbackRepo = feedbackRepo;
    }

    public AdminOverview getOverview() {
    	return new AdminOverview(
    			userRepo.count(),
    			productRepo.count(),
    			supplyChainLogRepo.count(),
    			feedbackRepo.count());
    }

}
