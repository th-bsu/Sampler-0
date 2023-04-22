package se.simple.microservices.core.recommendation.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import se.simple.api.core.recommendation.Recommendation;
import se.simple.api.core.recommendation.RecommendationService;
import se.simple.microservices.core.recommendation.persistence.RecommendationEntity;
import se.simple.microservices.core.recommendation.persistence.RecommendationRepository;
import se.simple.util.exceptions.InvalidInputException;
import se.simple.util.http.ServiceUtil;

@RestController
public class RecommendationServiceImpl implements RecommendationService {

    private static final Logger LOG = LoggerFactory.getLogger(RecommendationServiceImpl.class);

    private final RecommendationRepository repository;

    private final RecommendationMapper mapper;

    private final ServiceUtil serviceUtil;

    @Autowired
    public RecommendationServiceImpl(RecommendationRepository repository, RecommendationMapper mapper, ServiceUtil serviceUtil) {
        this.repository = repository;
        this.mapper = mapper;
        this.serviceUtil = serviceUtil;
    }//RecommendationServiceImpl.

    @Override
    public Recommendation createRecommendation(Recommendation body) {

        if (body.getProductId() < 1) throw new InvalidInputException("Invalid productId: " + body.getProductId());

        RecommendationEntity entity = mapper.apiToEntity(body);
        Mono<Recommendation> newEntity = repository.save(entity)
            .log()
            .onErrorMap(
                DuplicateKeyException.class,
                ex -> new InvalidInputException("Duplicate key, Product Id: " + body.getProductId() + ", Recommendation Id:" + body.getRecommendationId()))
            .map(e -> mapper.entityToApi(e));

        return newEntity.block();
        
    }//createRecommendation.

    @Override
    public Flux<Recommendation> getRecommendations(int productId) {

        if (productId < 1) throw new InvalidInputException("Invalid productId: " + productId);

        return repository.findByProductId(productId)
            .log()
            .map(e -> mapper.entityToApi(e))
            .map(e -> {e.setServiceAddress(serviceUtil.getServiceAddress()); return e;});
        
    }//getRecommendations: productId.
    
    // TH: gets recommendations by shelter.
    @Override
    public Flux<Recommendation> getRecommendationsByShelter(String shelter) {
        
        if (shelter.length() < 1) throw new InvalidInputException("Empty Input String !!");
        
        return repository.findByShelter(shelter)
            .log()
            .map(e -> mapper.entityToApi(e))
            .map(e -> {e.setServiceAddress(serviceUtil.getServiceAddress()); return e;});
        
    }//getRecommendationsByShelter: shelter.
    
    // TH: gets recommendations by (shelter,row).
    @Override
    public Flux<Recommendation> getRecommendationsByShelter(String shelter, String row) {
        
        if (shelter.length() < 1) throw new InvalidInputException("Empty Input String !!");
        
        /*
        // TH: original, working (i.e. probably slow).
        return repository.findByShelter(shelter,row)
            .log()
            .filter(e -> e.getRow() == Integer.parseInt(row))
            .map(e -> mapper.entityToApi(e))
            .map(e -> {e.setServiceAddress(serviceUtil.getServiceAddress()); return e;});
        */
        
        // TH: deploys Spring Framework utilities.
        // TH: https://docs.spring.io/spring-data/data-commons/docs/1.6.1.RELEASE/reference/html/repositories.html
        return repository.findByShelterAndRow(shelter,Integer.parseInt(row))
            .log()
            .map(e -> mapper.entityToApi(e))
            .map(e -> {e.setServiceAddress(serviceUtil.getServiceAddress()); return e;});
        
    }//getRecommendationsByShelter: shelter, row.
    
    @Override
    public void deleteRecommendations(int productId) {

        if (productId < 1) throw new InvalidInputException("Invalid productId: " + productId);

        LOG.debug("deleteRecommendations: tries to delete recommendations for the product with productId: {}", productId);
        repository.deleteAll(repository.findByProductId(productId)).block();
        
    }//deleteRecommendations: productId.
    
    @Override
    public void deleteRecommendations(Recommendation item) {
        
        if (item.getProductId() < 1) throw new InvalidInputException("Invalid productId: " + item.getProductId());
        LOG.debug("deleteRecommendations: tries to delete recommendations by: {}/{}/{}/{}", item.getShelter(), item.getRow(), item.getOffset(), item.getProductId());
        
        repository.deleteAll(
           repository.findByShelterAndRowAndOffsetAndProductId(
              item.getShelter(),
              item.getRow(),
              item.getOffset(),
              item.getProductId()
           )
        ).block();
        
    }//deleteRecommendations: item.
    
    @Override
    public void deleteRecommendations(int productId, int recommendationId) {
        
        if (productId < 1) throw new InvalidInputException("Invalid productId: " + productId);
        LOG.debug("deleteRecommendations: tries to delete recommendations for the product with: {}/{}", productId, recommendationId);
        
        repository.deleteAll(
           repository.findByProductIdAndRecommendationId(
              productId,
              recommendationId
           )
        ).block();
        
    }//deleteRecommendations: productId, recommendationId.
    
    @Override
    public void deleteRecommendations(String shelter, String recommendationId) {
        
        int recommendationId_ = Integer.parseInt(recommendationId);
        if (recommendationId_ <= 0) throw new InvalidInputException("Invalid recommendationId_: " + recommendationId_);
        LOG.debug("deleteRecommendations: tries to delete recommendations for the product with recommendationId_: {}", recommendationId_);
        
        repository.deleteAll(
           repository.findByShelterAndRecommendationId(
              shelter,
              recommendationId_
           )
        ).block();
        
    }//deleteRecommendations: recommendationId.
    
}

