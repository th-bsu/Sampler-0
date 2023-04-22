package se.simple.microservices.composite.product.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

import se.simple.api.composite.product.*;
import se.simple.api.core.product.Product;
import se.simple.api.core.recommendation.Recommendation;
import se.simple.api.core.review.Review;
import se.simple.util.http.ServiceUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.util.Arrays;

import java.security.SecureRandom;

// TH: helps map request data to request handler method.
// TH: helps convert response body to JSON or XML.
// TH: https://www.digitalocean.com/community/tutorials/spring-restcontroller
@RestController
public class ProductCompositeServiceImpl implements ProductCompositeService {

    private static final Logger LOG = LoggerFactory.getLogger(ProductCompositeServiceImpl.class);

    private final ServiceUtil serviceUtil;
    private final ProductCompositeIntegration integration;
    
    @Autowired
    public ProductCompositeServiceImpl(ServiceUtil serviceUtil, ProductCompositeIntegration integration) {
        this.serviceUtil = serviceUtil;
        this.integration = integration;
    }

    @Override
    public void createCompositeProduct(ProductAggregate body) {

        try {

            LOG.debug("createCompositeProduct: creates a new composite entity for productId: {}", body.getProductId());

            Product product = new Product(
               body.getProductId(), 
               body.getName(), 
               body.getAmount(), 
               serviceUtil.getServiceAddress(), 
               body.getValue(),
               body.getUrl()
            );
            integration.createProduct(product);

            if (body.getRecommendations() != null) {
                body.getRecommendations().forEach(r -> {
                    Recommendation recommendation = new Recommendation(
                       body.getProductId(), 
                       r.getRecommendationId(), 
                       r.getAuthor(), 
                       r.getRow(), 
                       r.getOffset(), 
                       null, 
                       r.getShelter(), 
                       r.getUrl()
                    );
                    integration.createRecommendation(recommendation);
                });
            }

            if (body.getReviews() != null) {
                body.getReviews().forEach(r -> {
                    Review review = new Review(body.getProductId(), r.getReviewId(), r.getAuthor(), r.getStatus(), r.getContent(), null);
                    integration.createReview(review);
                });
            }

            LOG.debug("createCompositeProduct: composite entities created for productId: {}", body.getProductId());

        } catch (RuntimeException re) {
            LOG.warn("createCompositeProduct failed: {}", re.toString());
            throw re;
        }
    }

    @Override
    public void ProductCompositeRecommendationAdd(ProductAggregate body) {
        
        try {
            
            LOG.debug("ProductCompositeRecommendationAdd: creates new recommendations for productId: {}", body.getProductId());
            
            if (body.getRecommendations() != null) {
                body.getRecommendations().forEach(r -> {
                    Recommendation recommendation = new Recommendation(
                       body.getProductId(), 
                       r.getRecommendationId(), 
                       r.getAuthor(), 
                       r.getRow(), 
                       r.getOffset(), 
                       null, 
                       r.getShelter(), 
                       r.getUrl()
                    );
                    integration.createRecommendation(recommendation);
                });
            }
            
        } catch (RuntimeException re) {
            LOG.warn("ProductCompositeRecommendationAdd Failed: {}", re.toString());
            throw re;
        }
        
    }

    @Override
    public Mono<ProductAggregate> getCompositeProduct(int productId) {
        return Mono.zip(
            values -> createProductAggregate(
               (Product) values[0], 
               (List<Recommendation>) values[1], 
               (List<Review>) values[2], 
               serviceUtil.getServiceAddress()
            ),
            integration.getProduct(productId),
            integration.getRecommendations(productId).collectList(),
            integration.getReviews(productId).collectList())
            .doOnError(ex -> LOG.warn("getCompositeProduct failed: {}", ex.toString()))
            .log();
    }
    
    @Override
    public Mono<ProductAggregate> getCompositeProductByShelter(String shelter){
        
        /*
        // TH: original.
        // TH: retrieves all product recommendations per shelter.
        integration.getRecommendationsByShelter(shelter);
        return null;
        */
        
        // TH: experimental.
        // TH: represents "dummy" product, primarily used to get all recommendations per shelter.
        int productId = 1;
        
        return Mono.zip(
               values -> createProductAggregate(
                  (Product) values[0], 
                  (List<Recommendation>) values[1], 
                  (List<Review>) values[2], 
                  serviceUtil.getServiceAddress()
               ),
               integration.getProduct(productId),
               integration.getRecommendationsByShelter(shelter).collectList(),
               integration.getReviews(productId).collectList()
            )
            .doOnError(ex -> LOG.warn("getCompositeProductByShelter failed: {}", ex.toString()))
            .log();
        
    }//getCompositeProductByShelter: shelter.
    
    private Mono<ProductAggregate> getCompositeProductByShelter(String shelter, String row){
        
        // TH: represents "dummy" product, primarily used to get all recommendations per (shelter,row).
        int productId = 1;
        
        return Mono.zip(
               values -> createProductAggregate(
                  (Product) values[0], 
                  (List<Recommendation>) values[1], 
                  (List<Review>) values[2], 
                  serviceUtil.getServiceAddress()
               ),
               integration.getProduct(productId),
               integration.getRecommendationsByShelter(shelter,row).collectList(),
               integration.getReviews(productId).collectList()
            )
            .doOnError(ex -> LOG.warn("getCompositeProductByShelter failed: {}", ex.toString()))
            .log();
        
    }//getCompositeProductByShelter: shelter, row.
    
    @Override
    public void deleteCompositeProduct(int productId) {

        try {

            LOG.debug("deleteCompositeProduct: Deletes a product aggregate for productId: {}", productId);

            integration.deleteProduct(productId);
            integration.deleteRecommendations(productId);
            integration.deleteReviews(productId);

            LOG.debug("deleteCompositeProduct: aggregate entities deleted for productId: {}", productId);

        } catch (RuntimeException re) {
            LOG.warn("deleteCompositeProduct failed: {}", re.toString());
            throw re;
        }
    }
    
    @Override
    public Mono<ProductAggregate> getCompositeProductByQueryShelter(String shelter) {
        return getCompositeProductByShelter(shelter);
    }
    
    @Override
    public Mono<ProductAggregate> getCompositeProductByQueryShelterRow(String shelter, String row) {
        return getCompositeProductByShelter(shelter,row);
    }
    
    // TH: deploys reactive programming.
    @Override
    public Integer getCompositeProductByInsertShelterRow(
        String shelter, 
        String row, 
        String index, 
        String side, 
        String productId
    ) {
        
        LOG.debug("getCompositeProductByInsertShelterRow: Started.");
        LOG.debug("side: {}", side);
        
        if(Integer.parseInt(side)!=INSERT_LEFT && Integer.parseInt(side)!=INSERT_RIGHT){
           LOG.debug("getCompositeProductByInsertShelterRow: Ended (Error).");
           return 1;
        }
        
        // TH: represents "dummy" product, primarily used to get all recommendations per (shelter,row).
        int productDummy = 1;
        
        // TH: represents new recommendationId.
        int recommendationId_new_Trial = (new SecureRandom()).nextInt(Integer.MAX_VALUE);
        while(recommendationId_new_Trial<=0){
           recommendationId_new_Trial = (new SecureRandom()).nextInt(Integer.MAX_VALUE);
        }//while <=0.
        
        final int recommendationId_new = recommendationId_new_Trial;
        
        /*
        // TODO: get URL by productId.
        String url = "Dummy-Right-Now";
        */
        
        // TH: retrieves existing recommendations per (shelter,row).
        // TH: https://stackoverflow.com/questions/57204225/java-reactor-flux-mono-when-does-doonnext-get-triggered-before-or-after-element
        integration.getRecommendationsByShelter(shelter,row)
        .doOnError(
                  ex -> {LOG.warn("getCompositeProductByInsertShelterRow Failed: {}", ex.toString());}
        )
        .doOnNext(
                  r -> {
                     
                     // TH: retrieves attributes.
                     int productId_        = r.getProductId();
                     String shelter_       = r.getShelter();
                     int row_              = r.getRow();
                     int offset_           = r.getOffset();
                     String url_           = r.getUrl();
                     
                     LOG.debug("productId_:        {}", productId_);
                     LOG.debug("shelter:           {}", shelter_);
                     LOG.debug("row:               {}", row_);
                     LOG.debug("offset (original): {}", offset_);
                     LOG.debug("url:               {}", url_);
                     
                     int recommendationId_ = r.getRecommendationId();
                     String author_        = r.getAuthor();
                     
                     // TH: avoids updating new item.
                     if(
                        productId_        == Integer.parseInt(productId) &&
                        recommendationId_ == recommendationId_new        
                     );
                     else{
                        
                        // TH: handles 'insert to left of anchor'.
                        if(Integer.parseInt(side)==INSERT_LEFT){
                           
                           if(offset_<Integer.parseInt(index)) {
                              ;
                           }//if less than AND excluding anchor.
                           
                           else{
                                 
                                 // TH: removes existing recommendation here.
                                 integration.deleteRecommendations(
                                    productId_,
                                    recommendationId_
                                 );
                                 
                                 // TH: updates attributes for insertion.
                                 offset_           = r.getOffset() + 1;
                                 recommendationId_ = (new SecureRandom()).nextInt(Integer.MAX_VALUE);
                                 while(recommendationId_<=0){
                                    recommendationId_ = (new SecureRandom()).nextInt(Integer.MAX_VALUE);
                                 }//while <=0.
                                 author_           = UUID.randomUUID().toString();
                                 
                                 LOG.debug("offset (updated):  {}", offset_);
                                 
                                 // TH: inserts new recommendation here.
                                 insertRecommendationsByAttributes(
                                    Integer.toString(productId_),
                                    Integer.toString(recommendationId_),
                                    author_,
                                    shelter_,
                                    Integer.toString(row_),
                                    Integer.toString(offset_),
                                    url_
                                 );
                                 
                           }//else: shifts right, including anchor.
                           
                        }//if insert left.
                        
                        // TH: handles 'insert to right of anchor'.
                        else if(Integer.parseInt(side)==INSERT_RIGHT){
                           
                           if(offset_<=Integer.parseInt(index)) {
                              ;
                           }//if less than AND including anchor.
                           
                           else{
                                 
                                 // TH: removes existing recommendation here.
                                 integration.deleteRecommendations(
                                    productId_,
                                    recommendationId_
                                 );
                                 
                                 // TH: updates attributes for insertion.
                                 offset_           = r.getOffset() + 1;
                                 recommendationId_ = (new SecureRandom()).nextInt(Integer.MAX_VALUE);
                                 while(recommendationId_<=0){
                                    recommendationId_ = (new SecureRandom()).nextInt(Integer.MAX_VALUE);
                                 }//while <=0.
                                 author_           = UUID.randomUUID().toString();
                                 
                                 LOG.debug("offset (updated):  {}", offset_);
                                 
                                 // TH: inserts new recommendation here.
                                 insertRecommendationsByAttributes(
                                    Integer.toString(productId_),
                                    Integer.toString(recommendationId_),
                                    author_,
                                    shelter_,
                                    Integer.toString(row_),
                                    Integer.toString(offset_),
                                    url_
                                 );
                                 
                           }//else: shifts right, excluding anchor.
                           
                        }//else if insert right.
                        
                     }//else: updates existing items.
                     
                  }//r.
        )//onNext.
        .doOnComplete(
                  () -> {
                     LOG.debug("getCompositeProductByInsertShelterRow: Ended, 0.");
                  }
        )//onComplete.
        .log()
        .subscribe()
        ;
        
        // TH: experimental --- dummy URL for now.
        String url = "https://cdn2.thecatapi.com/images/99r.jpg";
        
        // TH: instantiates Recommendation, if inserted next to anchor.
        Recommendation item_new = new Recommendation(
           Integer.parseInt(productId),
           recommendationId_new,
           UUID.randomUUID().toString(),
           Integer.parseInt(row),
           Integer.parseInt(side)==INSERT_LEFT ? Integer.parseInt(index) : Integer.parseInt(index)+1,
           serviceUtil.getServiceAddress(),
           shelter,
           url
        );
        
        integration.createRecommendation(item_new);
        
        LOG.debug("getCompositeProductByInsertShelterRow: Ended, 1.");
        return 0;
        
    }//getCompositeProductByInsertShelterRow.
    
    // TH: wraps around internal call (i.e. reactive programming).
    @Override
    public Integer getCompositeProductByRemoveShelterRow(
        String shelter,
        String row,
        String setSortedString,
        String setSortedBarString,
        String setTotalString
    ){
        
        LOG.debug("getCompositeProductByRemoveShelterRow: Started.");
        
        // TH: splits and collects non-empties.
        // TH: https://www.codevscolor.com/java-remove-empty-values-while-split
        String [] setSortedBarStringSplitted = Arrays.stream(setSortedBarString.split("AND")).filter(e -> e.trim().length() > 0).toArray(String[]::new);
        for(int index=0; index<setSortedBarStringSplitted.length; index++){
           LOG.debug("setSortedBarStringSplitted[{}]: {}", String.valueOf(index),setSortedBarStringSplitted[index]);
        }//for.
        
        // TH: collects final target offsets.
        String [] setTotalStringSplitted = Arrays.stream(setTotalString.split("AND")).filter(e -> e.trim().length() > 0).toArray(String[]::new);
        for(int index=0; index<setTotalStringSplitted.length; index++){
           LOG.debug("setTotalStringSplitted[{}]:     {}", String.valueOf(index),setTotalStringSplitted[index]);
        }//for.
        
        // TH: should have NOT happened !!
        if(setSortedBarStringSplitted.length!=setTotalStringSplitted.length) return 1;
        
        // TH: collects target rows.
        String [] rowSplitted = Arrays.stream(row.split("AND")).filter(e -> e.trim().length() > 0).toArray(String[]::new);
        for(int index=0; index<rowSplitted.length; index++){
           LOG.debug("rowSplitted[{}]:    {}", String.valueOf(index),rowSplitted[index]);
        }//for.
        
        // TH: collects target offsets to be deleted.
        String [] setSortedStringSplitted = Arrays.stream(setSortedString.split("AND")).filter(e -> e.trim().length() > 0).toArray(String[]::new);
        for(int index=0; index<setSortedStringSplitted.length; index++){
           LOG.debug("setSortedStringSplitted[{}]:    {}", String.valueOf(index),setSortedStringSplitted[index]);
        }//for.
        
        // TH: should have NOT happened !!
        if(rowSplitted.length!=setSortedStringSplitted.length) return 1;
        if(rowSplitted.length!=setSortedBarStringSplitted.length) return 1;
        if(rowSplitted.length!=setTotalStringSplitted.length) return 1;
        
        for(int index=0; index<rowSplitted.length; index++){
           
           getCompositeProductByRemoveShelterRowInternal(
              shelter,
              rowSplitted[index],
              setSortedStringSplitted[index],
              setSortedBarStringSplitted[index],
              setTotalStringSplitted[index]
           );
           
        }//for.
        
        LOG.debug("shelter:                        {}", shelter);
        LOG.debug("row:                            {}", row);
        LOG.debug("setSortedString:                {}", setSortedString);
        LOG.debug("setSortedBarString:             {}", setSortedBarString);
        LOG.debug("setTotalString:                 {}", setTotalString);
        
        LOG.debug("getCompositeProductByRemoveShelterRow: Ended.");
        return 0;
        
    }//getCompositeProductByRemoveShelterRow.
    
    // TH: deploys reactive programming.
    private Integer getCompositeProductByRemoveShelterRowInternal(
        String shelter,
        String row,
        String setSortedString,
        String setSortedBarString,
        String setTotalString
    ){
        
        LOG.debug("getCompositeProductByRemoveShelterRowInternal: Started.");
        
        // TH: collects initial target offsets.
        String [] setSortedBarStringSplitted = Arrays.stream(setSortedBarString.split("-")).filter(e -> e.trim().length() > 0).toArray(String[]::new);
        for(int index=0; index<setSortedBarStringSplitted.length; index++){
           LOG.debug("setSortedBarStringSplitted[{}]: {}", String.valueOf(index),setSortedBarStringSplitted[index]);
        }//for.
        
        // TH: collects final target offsets.
        String [] setTotalStringSplitted = Arrays.stream(setTotalString.split("-")).filter(e -> e.trim().length() > 0).toArray(String[]::new);
        for(int index=0; index<setTotalStringSplitted.length; index++){
           LOG.debug("setTotalStringSplitted[{}]:     {}", String.valueOf(index),setTotalStringSplitted[index]);
        }//for.
        
        // TH: should have NOT happened !!
        if(setSortedBarStringSplitted.length!=setTotalStringSplitted.length) return 1;
        
        // TH: collects target offsets to be deleted.
        String [] setSortedStringSplitted = Arrays.stream(setSortedString.split("-")).filter(e -> e.trim().length() > 0).toArray(String[]::new);
        for(int index=0; index<setSortedStringSplitted.length; index++){
           LOG.debug("setSortedStringSplitted[{}]:    {}", String.valueOf(index),setSortedStringSplitted[index]);
        }//for.
        
        // TH: helps with key-value pairs, for delete.
        Map<String,String> queue_delete = Collections.synchronizedMap(new HashMap<String,String>());
        for(int index=0; index<setSortedStringSplitted.length; index++){
           queue_delete.put(setSortedStringSplitted[index],setSortedStringSplitted[index]);
        }//for.
        
        // TH: splits and trims.
        row = Arrays.stream(row.split("-")).filter(e -> e.trim().length() > 0).toArray(String[]::new)[0];
        
        // TH: retrieves existing recommendations per (shelter,row).
        // TH: https://stackoverflow.com/questions/57204225/java-reactor-flux-mono-when-does-doonnext-get-triggered-before-or-after-element
        integration.getRecommendationsByShelter(shelter,row)
        .doOnError(
                  ex -> {LOG.warn("getCompositeProductByRemoveShelterRowInternal Failed: {}", ex.toString());}
        )
        .doOnNext(
                  r -> {
                     
                     // TH: retrieves attributes.
                     int productId_        = r.getProductId();
                     String shelter_       = r.getShelter();
                     int row_              = r.getRow();
                     String url_           = r.getUrl();
                     
                     int offset_           = r.getOffset();
                     int recommendationId_ = r.getRecommendationId();
                     
                     LOG.debug("productId_:        {}", productId_);
                     LOG.debug("shelter:           {}", shelter_);
                     LOG.debug("row:               {}", row_);
                     LOG.debug("url:               {}", url_);
                     
                     LOG.debug("offset (delete):   {}", offset_);
                     
                     if(queue_delete.containsKey(String.valueOf(offset_))) {
                        
                        // TH: removes existing recommendation here.
                        integration.deleteRecommendations(
                           productId_,
                           recommendationId_
                        );
                        
                     }//if contains.
                     else;
                     
                  }//r.
        )//onNext.
        .doOnComplete(
                  () -> {
                     LOG.debug("getCompositeProductByRemoveShelterRowInternal: Ended (delete).");
                  }
        )//onComplete.
        .log()
        .subscribe()
        ;
        
        // TH: helps with key-value pairs, for delete and insert.
        Map<String,String> queue_initial_final = Collections.synchronizedMap(new HashMap<String,String>());
        for(int index=0; index<setSortedBarStringSplitted.length; index++){
           queue_initial_final.put(setSortedBarStringSplitted[index],setTotalStringSplitted[index]);
        }//for.
        
        // TH: retrieves existing recommendations per (shelter,row).
        // TH: https://stackoverflow.com/questions/57204225/java-reactor-flux-mono-when-does-doonnext-get-triggered-before-or-after-element
        integration.getRecommendationsByShelter(shelter,row)
        .doOnError(
                  ex -> {LOG.warn("getCompositeProductByRemoveShelterRowInternal Failed: {}", ex.toString());}
        )
        .doOnNext(
                  r -> {
                     
                     // TH: retrieves attributes.
                     int productId_        = r.getProductId();
                     String shelter_       = r.getShelter();
                     int row_              = r.getRow();
                     String url_           = r.getUrl();
                     
                     int offset_           = r.getOffset();
                     int recommendationId_ = r.getRecommendationId();
                     
                     LOG.debug("productId_:        {}", productId_);
                     LOG.debug("shelter:           {}", shelter_);
                     LOG.debug("row:               {}", row_);
                     LOG.debug("url:               {}", url_);
                     
                     LOG.debug("offset (original): {}", offset_);
                     
                     if(queue_initial_final.containsKey(String.valueOf(offset_))) {
                        
                        // TH: removes existing recommendation here.
                        integration.deleteRecommendations(
                           productId_,
                           recommendationId_
                        );
                        
                        // TH: updates attributes for insertion.
                        int offset_final           = Integer.parseInt(queue_initial_final.get(String.valueOf(offset_)));
                        int recommendationId_final = (new SecureRandom()).nextInt(Integer.MAX_VALUE);
                        while(recommendationId_final<=0){
                           recommendationId_final = (new SecureRandom()).nextInt(Integer.MAX_VALUE);
                        }//while <=0.
                        String author_final        = UUID.randomUUID().toString();
                        
                        LOG.debug("offset (updated):  {}", offset_final);
                        
                        // TH: inserts new recommendation here.
                        insertRecommendationsByAttributes(
                           Integer.toString(productId_),
                           Integer.toString(recommendationId_final),
                           author_final,
                           shelter_,
                           Integer.toString(row_),
                           Integer.toString(offset_final),
                           url_
                        );
                        
                     }//if contains.
                     else;
                     
                  }//r.
        )//onNext.
        .doOnComplete(
                  () -> {
                     LOG.debug("getCompositeProductByRemoveShelterRowInternal: Ended (delete, insert).");
                  }
        )//onComplete.
        .log()
        .subscribe()
        ;
        
        LOG.debug("shelter:                        {}", shelter);
        LOG.debug("row:                            {}", row);
        LOG.debug("setSortedString:                {}", setSortedString);
        LOG.debug("setSortedBarString:             {}", setSortedBarString);
        LOG.debug("setTotalString:                 {}", setTotalString);
        
        LOG.debug("getCompositeProductByRemoveShelterRowInternal: Ended.");
        return 0;
        
    }//getCompositeProductByRemoveShelterRowInternal.
    
    // TH: wraps around internal call (i.e. reactive programming).
    @Override
    public Integer getCompositeProductBySubmitShelterRow(
        String shelter,
        String row,
        String setSortedStringProductId,
        String setSortedString,
        String setSortedBarString,
        String setTotalString,
        String setProductIdRemove
    ){
        
        // TH: collects target rows.
        // TH: https://www.codevscolor.com/java-remove-empty-values-while-split
        String [] rowSplitted = Arrays.stream(row.split("AND")).filter(e -> e.trim().length() > 0).toArray(String[]::new);
        
        // TH: collects target productIds to be added.
        String [] setSortedStringProductIdSplitted = Arrays.stream(setSortedStringProductId.split("AND")).filter(e -> e.trim().length() > 0).toArray(String[]::new);
        
        // TH: should have NOT happened (i.e. rows versus productIds, to be added) !!
        if(rowSplitted.length!=setSortedStringProductIdSplitted.length) return 1;
        
        // TH: collects target offsets to be added.
        String [] setSortedStringSplitted = Arrays.stream(setSortedString.split("AND")).filter(e -> e.trim().length() > 0).toArray(String[]::new);
        
        // TH: should have NOT happened (i.e. productIds versus offsets, to be added) !!
        if(setSortedStringProductIdSplitted.length!=setSortedStringSplitted.length) return 1;
        
        // TH: collects target recommendationIds of remaining elements.
        String [] setSortedBarStringSplitted = Arrays.stream(setSortedBarString.split("AND")).filter(e -> e.trim().length() > 0).toArray(String[]::new);
        
        // TH: should have NOT happened (i.e. new offsets versus remaining elements) !!
        if(setSortedStringSplitted.length!=setSortedBarStringSplitted.length) return 1;
        
        // TH: collects final indices of remaining elements.
        String [] setTotalStringSplitted = Arrays.stream(setTotalString.split("AND")).filter(e -> e.trim().length() > 0).toArray(String[]::new);
        
        // TH: should have NOT happened (i.e. recommendationIds versus final indices, remaining elements) !!
        if(setSortedBarStringSplitted.length!=setTotalStringSplitted.length) return 1;
        
        LOG.debug("getCompositeProductBySubmitShelterRow: Started.");
        
        // TH: displays statistics per row.
        for(int index=0; index<rowSplitted.length; index++){
           
           // TH: indicates NO need for insert.
           if(setSortedStringProductIdSplitted[index].equals(SETSORTEDSTRINGPRODUCTIDDUMMY)) continue;
           
           LOG.debug("rowSplitted[{}]:                         {}", String.valueOf(index),rowSplitted[index]);
           LOG.debug("setSortedStringProductIdSplitted[{}]:    {}", String.valueOf(index),setSortedStringProductIdSplitted[index]);
           LOG.debug("setSortedStringSplitted[{}]:             {}", String.valueOf(index),setSortedStringSplitted[index]);
           LOG.debug("setSortedBarStringSplitted[{}]:          {}", String.valueOf(index),setSortedBarStringSplitted[index]);
           LOG.debug("setTotalStringSplitted[{}]:              {}", String.valueOf(index),setTotalStringSplitted[index]);
           
           //  TH: issues insert and shift.
           getCompositeProductByInsertShelterRowInternal(
              shelter,
              rowSplitted[index],
              setSortedStringProductIdSplitted[index],
              setSortedStringSplitted[index],
              setSortedBarStringSplitted[index],
              setTotalStringSplitted[index]
           );
           
        }//for index.
        
        // TH: splits and collects ALL targets to be removed.
        // TH: https://www.codevscolor.com/java-remove-empty-values-while-split
        String [] setProductIdRemoveSplitted = Arrays.stream(setProductIdRemove.split("AND")).filter(e -> e.trim().length() > 0).toArray(String[]::new);
        for(int index=0; index<setProductIdRemoveSplitted.length; index++){
           LOG.debug("setProductIdRemoveSplitted[{}]: {}", String.valueOf(index),setProductIdRemoveSplitted[index]);
        }//for.
        
        String setProductIdRemoveSplittedString = setProductIdRemoveSplitted[0];
        LOG.debug("setProductIdRemoveSplittedString: {}", setProductIdRemoveSplittedString);
        
        // TH: splits and collects INDIVIDUAL targets to be removed.
        setProductIdRemoveSplitted = Arrays.stream(setProductIdRemoveSplittedString.split("-")).filter(e -> e.trim().length() > 0).toArray(String[]::new);
        for(int index=0; index<setProductIdRemoveSplitted.length; index++){
           LOG.debug("setProductIdRemoveSplitted[{}]: {}", String.valueOf(index),setProductIdRemoveSplitted[index]);
        }//for.
        
        // TH: removes target recommendationIds.
        for(int index=0; index<setProductIdRemoveSplitted.length; index++){
           getCompositeProductByRemoveShelterRowInternal(
              shelter,
              setProductIdRemoveSplitted[index]
           );
        }//for index.
        
        LOG.debug("shelter:                        {}", shelter);
        LOG.debug("row:                            {}", row);
        LOG.debug("setSortedStringProductId:       {}", setSortedStringProductId);
        LOG.debug("setSortedString:                {}", setSortedString);
        LOG.debug("setSortedBarString:             {}", setSortedBarString);
        LOG.debug("setTotalString:                 {}", setTotalString);
        LOG.debug("setProductIdRemove:             {}", setProductIdRemove);
        
        LOG.debug("getCompositeProductBySubmitShelterRow: Ended.");
        return 0;
        
    }//getCompositeProductBySubmitShelterRow.
    
    // TH: deploys reactive programming (i.e. insert, shift).
    private Integer getCompositeProductByInsertShelterRowInternal(
        String shelter,
        String row,
        String setSortedStringProductId,
        String setSortedString,
        String setSortedBarString,
        String setTotalString
    ){
        
        // TH: collects target productIds to be added.
        String [] setSortedStringProductIdSplitted = Arrays.stream(setSortedStringProductId.split("-")).filter(e -> e.trim().length() > 0).toArray(String[]::new);
        
        // TH: collects target offsets to be added.
        String [] setSortedStringSplitted = Arrays.stream(setSortedString.split("-")).filter(e -> e.trim().length() > 0).toArray(String[]::new);
        
        // TH: collects target recommendationIds of remaining elements.
        String [] setSortedBarStringSplitted = Arrays.stream(setSortedBarString.split("-")).filter(e -> e.trim().length() > 0).toArray(String[]::new);
        
        // TH: collects final indices of remaining elements.
        String [] setTotalStringSplitted = Arrays.stream(setTotalString.split("-")).filter(e -> e.trim().length() > 0).toArray(String[]::new);
        
        // TH: should have NOT happened (i.e. productIds versus offsets, to be added) !!
        if(setSortedStringProductIdSplitted.length!=setSortedStringSplitted.length) return 1;
        
        // TH: should have NOT happened (i.e. recommendationIds versus final indices, remaining elements) !!
        if(setSortedBarStringSplitted.length!=setTotalStringSplitted.length) return 1;
        
        LOG.debug("getCompositeProductByInsertShelterRowInternal: Started.");
        
        // TH: splits and trims.
        row = Arrays.stream(row.split("-")).filter(e -> e.trim().length() > 0).toArray(String[]::new)[0];
        LOG.debug("shelter:                        {}", shelter);
        LOG.debug("row:                            {}", row);
        
        // TH: represents remaining elements: recommendationId, offset.
        Map<String,String> queue_shift = Collections.synchronizedMap(new HashMap<String,String>());
        
        // TH: displays statistics for shift.
        for(int index=0; index<setSortedBarStringSplitted.length; index++){
           
           LOG.debug("setSortedBarStringSplitted[{}]: {}", String.valueOf(index),setSortedBarStringSplitted[index]);
           LOG.debug("setTotalStringSplitted[{}]:     {}", String.valueOf(index),setTotalStringSplitted[index]);
           queue_shift.put(setSortedBarStringSplitted[index],setTotalStringSplitted[index]);
           
        }//for.
        
        // TH: retrieves existing recommendations per (shelter,row).
        // TH: https://stackoverflow.com/questions/57204225/java-reactor-flux-mono-when-does-doonnext-get-triggered-before-or-after-element
        integration.getRecommendationsByShelter(shelter,row)
        .doOnError(
           ex -> {LOG.warn("getCompositeProductByInsertShelterRowInternal Failed (0): {}", ex.toString());}
        )
        .doOnNext(
           
           r -> {
              
              // TH: retrieves attributes.
              int productId_        = r.getProductId();
              int recommendationId_ = r.getRecommendationId();
              int row_              = r.getRow();
              String url_           = r.getUrl();
              
              LOG.debug("productId_:        {}", productId_);
              LOG.debug("recommendationId_: {}", recommendationId_);
              
              if(queue_shift.containsKey(String.valueOf(recommendationId_))) {
                 
                 // TH: removes existing recommendation here.
                 integration.deleteRecommendations(
                    productId_,
                    recommendationId_
                 );
                 
                 // TH: updates attributes for insertion.
                 int recommendationIdNew = (new SecureRandom()).nextInt(Integer.MAX_VALUE);
                 while(recommendationIdNew<=0){
                    recommendationIdNew  = (new SecureRandom()).nextInt(Integer.MAX_VALUE);
                 }//while <=0.
                 
                 int offsetNew           = Integer.parseInt(queue_shift.get(String.valueOf(recommendationId_)));
                 
                 // TH: inserts new recommendation here.
                 insertRecommendationsByAttributes(
                    Integer.toString(productId_),
                    Integer.toString(recommendationIdNew),
                    UUID.randomUUID().toString(),
                    shelter,
                    Integer.toString(row_),
                    Integer.toString(offsetNew),
                    url_
                 );
                 
              }//if contains.
              else;
              
           }//r.
           
        )
        .doOnComplete(
           () -> {
              LOG.debug("getCompositeProductByInsertShelterRowInternal: Ended (shift).");
           }
        )//onComplete.
        .log()
        .subscribe()
        ;
        
        // TH: displays statistics for insert.
        for(int index=0; index<setSortedStringProductIdSplitted.length; index++){
           
           LOG.debug("setSortedStringProductIdSplitted[{}]:    {}", String.valueOf(index),setSortedStringProductIdSplitted[index]);
           LOG.debug("setSortedStringSplitted[{}]:             {}", String.valueOf(index),setSortedStringSplitted[index]);
           
           int recommendationId = (new SecureRandom()).nextInt(Integer.MAX_VALUE);
           while(recommendationId<=0){
              recommendationId  = (new SecureRandom()).nextInt(Integer.MAX_VALUE);
           }//while <=0.
           
           /*
           // TH: original --- dummy URL for now.
           String url = "https://cdn2.thecatapi.com/images/99r.jpg";
           
           // TH: inserts new recommendation here.
           insertRecommendationsByAttributes(
              setSortedStringProductIdSplitted[index],
              Integer.toString(recommendationId),
              UUID.randomUUID().toString(),
              shelter,
              row,
              setSortedStringSplitted[index],
              url // null
           );
           */
           
           final int indexFinal            = index;
           final int recommendationIdFinal = recommendationId;
           final String rowFinal           = row;
           
           // TH: indicates dummy URL for now.
           final String url                = "https://cdn2.thecatapi.com/images/99r.jpg";
           
           // TH: experimental --- retrieves URL from productId.
           integration.getProduct(Integer.parseInt(setSortedStringProductIdSplitted[index]))
           .doOnError(
              ex -> {
                 
                 LOG.warn("getCompositeProductByInsertShelterRowInternal Failed (1): {}", ex.toString());
                 LOG.warn("getCompositeProductByInsertShelterRowInternal Failed (1 - Dummy URL): {}", url);
                 
                 // TH: inserts new recommendation here.
                 insertRecommendationsByAttributes(
                    setSortedStringProductIdSplitted[indexFinal],
                    Integer.toString(recommendationIdFinal),
                    UUID.randomUUID().toString(),
                    shelter,
                    rowFinal,
                    setSortedStringSplitted[indexFinal],
                    url // null
                 );
                 
              }
           )
           .doOnNext(
              
              r -> {
                 
                 // TH: inserts new recommendation here.
                 insertRecommendationsByAttributes(
                    setSortedStringProductIdSplitted[indexFinal],
                    Integer.toString(recommendationIdFinal),
                    UUID.randomUUID().toString(),
                    shelter,
                    rowFinal,
                    setSortedStringSplitted[indexFinal],
                    r.getUrl()
                 );
                 
              }//r.
              
           )
           .doOnSuccess(
              
              success -> {
                 LOG.debug("getCompositeProductByInsertShelterRowInternal: Ended (insert).");
              }
              
           )
           .log()
           .subscribe()
           ;
           
        }//for.
        
        LOG.debug("getCompositeProductByInsertShelterRowInternal: Ended.");
        return 0;
        
    }//getCompositeProductByInsertShelterRowInternal.
    
    
    // TH: deploys reactive programming (i.e. remove).
    private void getCompositeProductByRemoveShelterRowInternal(
        String shelter,
        String setProductIdRemove
    ){
        
        LOG.debug("getCompositeProductByRemoveShelterRowInternal: Started.");
        
        // TH: removes existing recommendation here.
        integration.deleteRecommendations(
           shelter,
           setProductIdRemove
        );
        
        LOG.debug("shelter:                        {}", shelter);
        LOG.debug("setProductIdRemove:             {}", setProductIdRemove);
        
        LOG.debug("getCompositeProductByRemoveShelterRowInternal: Ended.");
        return;
        
    }//getCompositeProductByRemoveShelterRowInternal.
    
    @Override
    public Integer deleteRecommendationsByRecommendationId(String productId, String recommendationId) {
        
        // TH: removes existing recommendations.
        integration.deleteRecommendations(
           Integer.parseInt(productId),
           Integer.parseInt(recommendationId)
        );
        
        return 0;
        
    }//deleteRecommendationsByRecommendationId.
    
    @Override
    public Integer insertRecommendationsByAttributes(
        String productId,
        String recommendationId,
        String author,
        String shelter,
        String row,
        String offset,
        String url
    ) {
       
       // TH: instantiates Recommendation, if inserted next to anchor.
       Recommendation item_ = new Recommendation(
          Integer.parseInt(productId),
          Integer.parseInt(recommendationId),
          author,
          Integer.parseInt(row),
          Integer.parseInt(offset),
          serviceUtil.getServiceAddress(),
          shelter,
          url
       );
       
       // TH: inserts new recommendation here.
       integration.createRecommendation(item_);
       
       return 0;
       
    }//insertRecommendationsByAttributes.
    
    private ProductAggregate createProductAggregate(Product product, List<Recommendation> recommendations, List<Review> reviews, String serviceAddress) {

        // 1. Setup product info
        int productId = product.getProductId();
        String name = product.getName();
        int weight = product.getAmount();
        double value = product.getValue();
        String url = product.getUrl();

        // 2. Copy summary recommendation info, if available
        List<RecommendationSummary> recommendationSummaries = (recommendations == null) ? null :
             recommendations.stream()
                .map(
                   r -> new RecommendationSummary(
                      r.getRecommendationId(), 
                      r.getAuthor(), 
                      r.getRow(), 
                      r.getOffset(), 
                      r.getShelter(), 
                      r.getProductId(), 
                      r.getUrl()
                   )
                )
                .collect(Collectors.toList());

        // 3. Copy summary review info, if available
        List<ReviewSummary> reviewSummaries = (reviews == null)  ? null :
            reviews.stream()
                .map(r -> new ReviewSummary(r.getReviewId(), r.getAuthor(), r.getSubject(), r.getContent()))
                .collect(Collectors.toList());

        // 4. Create info regarding the involved microservices addresses
        String productAddress = product.getServiceAddress();
        String reviewAddress = (reviews != null && reviews.size() > 0) ? reviews.get(0).getServiceAddress() : "";
        String recommendationAddress = (recommendations != null && recommendations.size() > 0) ? recommendations.get(0).getServiceAddress() : "";
        ServiceAddresses serviceAddresses = new ServiceAddresses(serviceAddress, productAddress, reviewAddress, recommendationAddress);

        return new ProductAggregate(productId, name, weight, value, recommendationSummaries, reviewSummaries, serviceAddresses, url);
    }
    
}
