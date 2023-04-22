package se.simple.microservices.core.recommendation.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;
import se.simple.api.core.recommendation.Recommendation;
import se.simple.api.core.recommendation.RecommendationService;
import se.simple.api.event.Event;
import se.simple.util.exceptions.EventProcessingException;

@EnableBinding(Sink.class)
public class MessageProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(MessageProcessor.class);

    private final RecommendationService recommendationService;

    @Autowired
    public MessageProcessor(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @StreamListener(target = Sink.INPUT)
    public void process(Event<Integer, Recommendation> event) {

        LOG.info("Process message created at {}...", event.getEventCreatedAt());

        switch (event.getEventType()) {

        case CREATE:
            Recommendation recommendation = event.getData();
            LOG.info("Create recommendation with ID: {}/{}", recommendation.getProductId(), recommendation.getRecommendationId());
            recommendationService.createRecommendation(recommendation);
            break;

        case DELETE:
            int productId = event.getKey();
            LOG.info("Delete recommendations with ProductID: {}", productId);
            recommendationService.deleteRecommendations(productId);
            break;
        
        case DELETE_V2:
            Recommendation item = event.getData();
            LOG.info("Delete recommendation: {}/{}/{}/{}", item.getShelter(), item.getRow(), item.getOffset(), item.getProductId());
            recommendationService.deleteRecommendations(item);
            break;
        
        case DELETE_V3:
            Recommendation item_ = event.getData();
            LOG.info("Delete recommendations with: {}/{}", item_.getProductId(), item_.getRecommendationId());
            recommendationService.deleteRecommendations(item_.getProductId(), item_.getRecommendationId());
            break;
        
        case DELETE_V4:
            Recommendation item__ = event.getData();
            LOG.info("Delete recommendations with RecommendationId: {} at shelter {}", item__.getRecommendationId(), item__.getShelter());
            recommendationService.deleteRecommendations(item__.getShelter(),String.valueOf(item__.getRecommendationId()));
            break;
        
        default:
            String errorMessage = "Incorrect event type: " + event.getEventType() + ", expected a CREATE or DELETE event";
            LOG.warn(errorMessage);
            throw new EventProcessingException(errorMessage);
        }

        LOG.info("Message processing done!");
    }
}
