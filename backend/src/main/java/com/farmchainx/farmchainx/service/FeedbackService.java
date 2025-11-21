package com.farmchainx.farmchainx.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.farmchainx.farmchainx.dto.FeedbackRequest;
import com.farmchainx.farmchainx.model.Feedback;
import com.farmchainx.farmchainx.repository.FeedbackRepository;
import com.farmchainx.farmchainx.repository.ProductRepository;

@Service
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final ProductRepository productRepository;

    public FeedbackService(FeedbackRepository feedbackRepository, ProductRepository productRepository) {
        this.feedbackRepository = feedbackRepository;
        this.productRepository = productRepository;
    }

    public Feedback addFeedback(Long productId, Long consumerId, FeedbackRequest feedback) {

        productRepository.findById(productId)
            .orElseThrow(() -> new RuntimeException("Product is not available"));

        if (feedback.getRating() < 1 || feedback.getRating() > 5) {
            throw new RuntimeException("Rating must be 1 to 5");
        }

        if (feedbackRepository.existsByProductIdAndConsumerId(productId, consumerId)) {
            throw new RuntimeException("You have already submitted the feedback for this product");
        }

        Feedback f = new Feedback();
        f.setProductId(productId);
        f.setConsumerId(consumerId);
        f.setRating(feedback.getRating());
        String comment = feedback.getComment() == null ? "" : feedback.getComment().trim();
        f.setComment(comment);
        f.setCreatedAt(LocalDateTime.now()); 

        return feedbackRepository.save(f);
    }

    public List<Feedback> getFeedbackForProduct(Long productId) {
        return feedbackRepository.findByProductId(productId);
    }
}
