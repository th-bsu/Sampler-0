package se.simple.microservices.composite.product.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import se.simple.api.core.product.Product;
import se.simple.api.core.product.ProductService;
import se.simple.api.core.recommendation.Recommendation;
import se.simple.api.core.recommendation.RecommendationService;
import se.simple.api.core.review.Review;
import se.simple.api.core.review.ReviewService;
import se.simple.api.event.Event;
import se.simple.util.exceptions.InvalidInputException;
import se.simple.util.exceptions.NotFoundException;
import se.simple.util.http.HttpErrorInfo;

import java.io.IOException;

import static reactor.core.publisher.Flux.empty;
import static se.simple.api.event.Event.Type.CREATE;
import static se.simple.api.event.Event.Type.DELETE;
import static se.simple.api.event.Event.Type.DELETE_V2;
import static se.simple.api.event.Event.Type.DELETE_V3;
import static se.simple.api.event.Event.Type.DELETE_V4;

// TH: helps connect to messaging microservices, either locally or on cloud.
// TH: https://docs.spring.io/spring-cloud-dataflow/docs/1.0.0.M1/reference/html/_introducing_spring_cloud_stream.html
// TH: represents component to be instantiated (with dependency) and injected into wherever needed.
// TH: https://www.baeldung.com/spring-component-annotation
@EnableBinding(ProductCompositeIntegration.MessageSources.class)
@Component
public class ProductCompositeIntegration implements ProductService, RecommendationService, ReviewService {

    private static final Logger LOG = LoggerFactory.getLogger(ProductCompositeIntegration.class);

    private final String productServiceUrl = "http://product";
    private final String recommendationServiceUrl = "http://recommendation";
    private final String reviewServiceUrl = "http://review";

    private final ObjectMapper mapper;
    private final WebClient.Builder webClientBuilder;

    private WebClient webClient;

    private MessageSources messageSources;

    public interface MessageSources {

        // TH: references spring.cloud.stream.bindings.output-products.destination in application.yml.
        // TH: references spring.cloud.stream.bindings.input.destination in application.yml.
        String OUTPUT_PRODUCTS = "output-products";
        
        String OUTPUT_RECOMMENDATIONS = "output-recommendations";
        String OUTPUT_REVIEWS = "output-reviews";

        // TH: specifies output channel.
        @Output(OUTPUT_PRODUCTS)
        MessageChannel outputProducts();

        @Output(OUTPUT_RECOMMENDATIONS)
        MessageChannel outputRecommendations();

        @Output(OUTPUT_REVIEWS)
        MessageChannel outputReviews();
    }

    @Autowired
    public ProductCompositeIntegration(
        WebClient.Builder webClientBuilder,
        ObjectMapper mapper,
        MessageSources messageSources
    ) {
        this.webClientBuilder = webClientBuilder;
        this.mapper = mapper;
        this.messageSources = messageSources;
    }

    @Override
    public Product createProduct(Product body) {
        // TH: sends message to given channel.
        messageSources.outputProducts().send(MessageBuilder.withPayload(new Event(CREATE, body.getProductId(), body)).build());
        return body;
    }

    @Override
    public Mono<Product> getProduct(int productId) {
        String url = productServiceUrl + "/product/" + productId;
        LOG.debug("Will call the getProduct API on URL: {}", url);

        return getWebClient().get().uri(url).retrieve().bodyToMono(Product.class).log().onErrorMap(WebClientResponseException.class, ex -> handleException(ex));
    }

    @Override
    public void deleteProduct(int productId) {
        messageSources.outputProducts().send(MessageBuilder.withPayload(new Event(DELETE, productId, null)).build());
    }

    @Override
    public Recommendation createRecommendation(Recommendation body) {
        messageSources.outputRecommendations().send(MessageBuilder.withPayload(new Event(CREATE, body.getProductId(), body)).build());
        return body;
    }

    @Override
    public Flux<Recommendation> getRecommendations(int productId) {

        String url = recommendationServiceUrl + "/recommendation?productId=" + productId;

        LOG.debug("Will call the getRecommendations API on URL: {}", url);

        // Return an empty result if something goes wrong to make it possible for the composite service to return partial responses
        return getWebClient().get().uri(url).retrieve().bodyToFlux(Recommendation.class).log().onErrorResume(error -> empty());
    }

    @Override
    public Flux<Recommendation> getRecommendationsByShelter(String shelter) {
        
        // TH: specifies shelter.
        String url = recommendationServiceUrl + "/recommendation-by-shelter?shelter=" + shelter;
        
        LOG.debug("Will call the getRecommendationsByShelter API on URL: {}", url);
        
        // TH: accepts empty result from core service (i.e. failure) and returns partial result from composite service.
        return getWebClient().get().uri(url).retrieve().bodyToFlux(Recommendation.class).log().onErrorResume(error -> empty());
        
    }
    
    @Override
    public Flux<Recommendation> getRecommendationsByShelter(String shelter, String row) {
        
        // TH: specifies shelter.
        String url = recommendationServiceUrl + "/recommendation-by-shelter-row?shelter=" + shelter + "&row=" + row;
        
        LOG.debug("Will call the getRecommendationsByShelter API on URL: {}", url);
        
        // TH: accepts empty result from core service (i.e. failure) and returns partial result from composite service.
        return getWebClient().get().uri(url).retrieve().bodyToFlux(Recommendation.class).log().onErrorResume(error -> empty());
        
    }
    
    @Override
    public void deleteRecommendations(int productId) {
        
        // TH: invokes MessageProcessor: DELETE.
        messageSources.outputRecommendations().send(MessageBuilder.withPayload(new Event(DELETE, productId, null)).build());
        
    }//deleteRecommendations: productId.
    
    @Override
    public void deleteRecommendations(int productId, int recommendationId) {
        
        // TH: instantiates Recommendation, if inserted next to anchor.
        Recommendation item_ = new Recommendation(
           productId,
           recommendationId,
           null,
           -1,
           -1,
           null,
           null,
           null
        );
        
        // TH: invokes MessageProcessor: DELETE_V3.
        messageSources.outputRecommendations().send(MessageBuilder.withPayload(new Event(DELETE_V3, null, item_)).build());
        
    }//deleteRecommendations: productId, recommendationId.
    
    @Override
    public void deleteRecommendations(Recommendation item) {
        
        // TH: invokes MessageProcessor: DELETE_V2.
        messageSources.outputRecommendations().send(MessageBuilder.withPayload(new Event(DELETE_V2, null, item)).build());
        
    }//deleteRecommendations: item.
    
    @Override
    public void deleteRecommendations(String shelter, String recommendationId) {
        
        // TH: instantiates Recommendation, if inserted next to anchor.
        Recommendation item_ = new Recommendation(
           -1,
           Integer.parseInt(recommendationId),
           null,
           -1,
           -1,
           null,
           shelter,
           null
        );
        
        // TH: invokes MessageProcessor: DELETE_V4.
        messageSources.outputRecommendations().send(MessageBuilder.withPayload(new Event(DELETE_V4, null, item_)).build());
        
    }//deleteRecommendations: recommendationId.
    
    @Override
    public Review createReview(Review body) {
        messageSources.outputReviews().send(MessageBuilder.withPayload(new Event(CREATE, body.getProductId(), body)).build());
        return body;
    }

    @Override
    public Flux<Review> getReviews(int productId) {

        String url = reviewServiceUrl + "/review?productId=" + productId;

        LOG.debug("Will call the getReviews API on URL: {}", url);

        // Return an empty result if something goes wrong to make it possible for the composite service to return partial responses
        return getWebClient().get().uri(url).retrieve().bodyToFlux(Review.class).log().onErrorResume(error -> empty());

    }

    @Override
    public void deleteReviews(int productId) {
        messageSources.outputReviews().send(MessageBuilder.withPayload(new Event(DELETE, productId, null)).build());
    }

    private WebClient getWebClient() {
        if (webClient == null) {
            webClient = webClientBuilder.build();
        }
        return webClient;
    }

    private Throwable handleException(Throwable ex) {

        if (!(ex instanceof WebClientResponseException)) {
            LOG.warn("Got a unexpected error: {}, will rethrow it", ex.toString());
            return ex;
        }

        WebClientResponseException wcre = (WebClientResponseException)ex;

        switch (wcre.getStatusCode()) {

        case NOT_FOUND:
            return new NotFoundException(getErrorMessage(wcre));

        case UNPROCESSABLE_ENTITY :
            return new InvalidInputException(getErrorMessage(wcre));

        default:
            LOG.warn("Got a unexpected HTTP error: {}, will rethrow it", wcre.getStatusCode());
            LOG.warn("Error body: {}", wcre.getResponseBodyAsString());
            return ex;
        }
    }

    private String getErrorMessage(WebClientResponseException ex) {
        try {
            return mapper.readValue(ex.getResponseBodyAsString(), HttpErrorInfo.class).getMessage();
        } catch (IOException ioex) {
            return ex.getMessage();
        }
    }
}
