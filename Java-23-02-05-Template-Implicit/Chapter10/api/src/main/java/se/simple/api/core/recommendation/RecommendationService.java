package se.simple.api.core.recommendation;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

public interface RecommendationService {
    
    // TH: creates Recommendation by given instance (i.e. see 'delete').
    Recommendation createRecommendation
       (@RequestBody Recommendation body
    );

    /**
     * Sample usage:
     *
     * curl $HOST:$PORT/recommendation?productId=1
     *
     * @param productId
     * @return
     */
    @GetMapping(
        value    = "/recommendation",
        produces = "application/json")
    Flux<Recommendation> getRecommendations(
       @RequestParam(value = "productId", required = true) int productId
    );

    /**
     * TH: helps retrieve all product recommendations per shelter.
     * Sample usage:
     *
     * curl $HOST:$PORT/recommendation-by-shelter?shelter=19-02
     *
     * @param shelter
     * @return all product recommendations per shelter.
     */
    @GetMapping(
        value    = "/recommendation-by-shelter",
        produces = "application/json")
    Flux<Recommendation> getRecommendationsByShelter(
        @RequestParam(value = "shelter", required = true) String shelter
    );
    
    /**
     * TH: helps retrieve all product recommendations per (shelter,row).
     * Sample usage:
     *
     * curl $HOST:$PORT/recommendation-by-shelter-row?shelter=22-01&row=5
     *
     * @param shelter
     * @param row
     * @return all product recommendations per (shelter,row).
     */
    @GetMapping(
        value    = "/recommendation-by-shelter-row",
        produces = "application/json")
    Flux<Recommendation> getRecommendationsByShelter(
        @RequestParam(value = "shelter", required = true) String shelter,
        @RequestParam(value = "row",     required = true) String row
    );
    
    // TH: deletes ALL recommendations by given attribute.
    void deleteRecommendations(
       @RequestParam(value = "productId", required = true)  int productId
    );
    
    // TH: deletes ALL recommendations by given attributes.
    void deleteRecommendations(
       @RequestParam(value = "productId", required = true)         int productId,
       @RequestParam(value = "recommendationId", required = true)  int recommendationId
    );
    
    // TH: delete Recommendation by given instance (i.e. see 'create').
    void deleteRecommendations(
       @RequestBody Recommendation item
    );
    
    // TH: deletes ALL recommendations by given attribute.
    void deleteRecommendations(
       @RequestParam(value = "shelter", required = true)           String shelter,
       @RequestParam(value = "recommendationId", required = true)  String recommendationId
    );
    
}

