package se.simple.microservices.core.recommendation.persistence;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.data.mongodb.core.query.Query;
import reactor.core.publisher.Flux;

public interface RecommendationRepository extends ReactiveCrudRepository<RecommendationEntity, String> {
    
    // TH: issues query by productId.
    Flux<RecommendationEntity> findByProductId(int productId);
    
    // TH: issues query by shelter.
    Flux<RecommendationEntity> findByShelter(String shelter);
    
    // TH: issues query by given attributes.
    Flux<RecommendationEntity> findByShelterAndRow(
       String shelter,
       int row
    );
    
    // TH: issues query by given attributes.
    Flux<RecommendationEntity> findByShelterAndRowAndOffsetAndProductId(
              String shelter,
              int row,
              int offset,
              int productId
    );
    
    // TH: issues query by given attributes.
    Flux<RecommendationEntity> findByProductIdAndRecommendationId(
       int productId,
       int recommendationId
    );
    
    // TH: issues query by given recommendationId.
    Flux<RecommendationEntity> findByShelterAndRecommendationId(
       String shelter,
       int recommendationId
    );
    
}

