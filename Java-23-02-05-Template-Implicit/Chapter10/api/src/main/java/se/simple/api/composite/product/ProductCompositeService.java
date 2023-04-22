package se.simple.api.composite.product;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.web.bind.annotation.*;

import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

import java.util.List;

@Api(description = "REST API for composite product information.")
public interface ProductCompositeService {
    
    public static final int INSERT_LEFT  = 0;
    public static final int INSERT_RIGHT = 1;
    
    // IMPORTANT: DO NOT USE productId == 1 !!
    
    public static final String SETSORTEDSTRINGPRODUCTIDDUMMY = "setSortedStringProductIdDummy";
    
    /**
     * Sample usage:
     *
     * curl -X POST $HOST:$PORT/product-composite   \
     *   -H "Content-Type: application/json" --data \
     *   '{"name":"A","amount":78, "value":100.000, "reviews":[...], "recommendations":[...]}'
     *
     * @param body
     */
    @ApiOperation(
        value = "${api.product-composite.create-composite-product.description}",
        notes = "${api.product-composite.create-composite-product.notes}")
    @ApiResponses(value = {
        @ApiResponse(code = 400, message = "Bad Request, invalid format of the request. See response message for more information."),
        @ApiResponse(code = 422, message = "Unprocessable entity, input parameters caused the processing to fail. See response message for more information.")
    })
    @PostMapping(
        value    = "/product-composite",
        consumes = "application/json")
    void createCompositeProduct(@RequestBody ProductAggregate body);

    /**
     * Sample usage: curl $HOST:$PORT/product-composite/1
     *
     * @param productId
     * @return the composite product info, if found, else null
     */
    @ApiOperation(
        value = "${api.product-composite.get-composite-product.description}",
        notes = "${api.product-composite.get-composite-product.notes}")
    @ApiResponses(value = {
        @ApiResponse(code = 400, message = "Bad Request, invalid format of the request. See response message for more information."),
        @ApiResponse(code = 404, message = "Not found, the specified id does not exist."),
        @ApiResponse(code = 422, message = "Unprocessable entity, input parameters caused the processing to fail. See response message for more information.")
    })
    @GetMapping(
        value    = "/product-composite/{productId}",
        produces = "application/json")
    Mono<ProductAggregate> getCompositeProduct(@PathVariable int productId);

    /**
     * Sample usage:
     *
     * curl -X DELETE $HOST:$PORT/product-composite/1
     *
     * @param productId
     */
    @ApiOperation(
        value = "${api.product-composite.delete-composite-product.description}",
        notes = "${api.product-composite.delete-composite-product.notes}")
    @ApiResponses(value = {
        @ApiResponse(code = 400, message = "Bad Request, invalid format of the request. See response message for more information."),
        @ApiResponse(code = 422, message = "Unprocessable entity, input parameters caused the processing to fail. See response message for more information.")
    })
    @DeleteMapping(value = "/product-composite/{productId}")
    void deleteCompositeProduct(@PathVariable int productId);
    
    /**
     * TH: helps retrieve all product recommendations per shelter.
     * Sample usage: curl $HOST:$PORT/product-composite-by-shelter/19-02
     *
     * @param shelter
     * @return all product recommendations per shelter.
     */
    @ApiOperation(
        value = "${api.product-composite.get-composite-product-by-shelter.description}",
        notes = "${api.product-composite.get-composite-product-by-shelter.notes}")
    @ApiResponses(value = {
        @ApiResponse(code = 400, message = "BAD REQUEST, invalid request format."),
        @ApiResponse(code = 404, message = "NOT FOUND, shelter ID does NOT exist."),
        @ApiResponse(code = 422, message = "UNPROCESSABLE ENTITY, input parameters invalid.")
    })
    @GetMapping(
        value    = "/product-composite-by-shelter/{shelter}",
        produces = "application/json")
    Mono<ProductAggregate> getCompositeProductByShelter(@PathVariable String shelter);
    
    /**
     * TH: helps add recommendations to given product.
     * Sample usage:
     *
     * curl -X POST $HOST:$PORT/product-composite-recommendation-add \
     *   -H "Content-Type: application/json" --data                  \
     *   '{"productId\":15717",
     *     "recommendations":[
     *         {"recommendationId":1,"author":"author 1","row":0,"offset":6, "shelter":"22-01", "url":"https://cdn2.thecatapi.com/images/15r.jpg"},
     *         {"recommendationId":2,"author":"author 2","row":0,"offset":7, "shelter":"22-01", "url":"https://cdn2.thecatapi.com/images/15r.jpg"}
     *      ]
     *    }'
     * 
     * @param body
     */
    @ApiOperation(
        value = "${api.product-composite.product-composite-recommendation-add.description}",
        notes = "${api.product-composite.product-composite-recommendation-add.notes}")
    @ApiResponses(value = {
        @ApiResponse(code = 400, message = "Bad Request, invalid format of the request. See response message for more information."),
        @ApiResponse(code = 404, message = "Not found, the specified id does not exist."),
        @ApiResponse(code = 422, message = "Unprocessable entity, input parameters caused the processing to fail. See response message for more information.")
    })
    @PostMapping(
        value    = "/product-composite-recommendation-add",
        consumes = "application/json")
    void ProductCompositeRecommendationAdd(@RequestBody ProductAggregate body);
    
    /**
     * TH: helps retrieve all product recommendations per shelter, by query.
     * TH: curl http://localhost:8080/product-composite-by-query-shelter/?shelter=22-01
     * TH: https://www.baeldung.com/spring-request-param
     */
    @ApiOperation(
        value = "${api.product-composite.get-composite-product-by-query-shelter.description}",
        notes = "${api.product-composite.get-composite-product-by-query-shelter.notes}")
    @ApiResponses(value = {
        @ApiResponse(code = 400, message = "BAD REQUEST, invalid request format."),
        @ApiResponse(code = 404, message = "NOT FOUND, shelter ID does NOT exist."),
        @ApiResponse(code = 422, message = "UNPROCESSABLE ENTITY, input parameters invalid.")
    })
    @GetMapping(
        value    = "/product-composite-by-query-shelter",
        produces = "application/json"
    )
    @ResponseBody
    Mono<ProductAggregate> getCompositeProductByQueryShelter(@RequestParam(defaultValue = "00-00") String shelter);
    
    /**
     * TH: helps retrieve all product recommendations per (shelter,row), by QUERY.
     * TH: curl http://localhost:8080/product-composite-by-query-shelter-row/?shelter=22-01&row=5
     * TH: https://www.baeldung.com/spring-request-param
     */
    @ApiOperation(
        value = "${api.product-composite.get-composite-product-by-query-shelter-row.description}",
        notes = "${api.product-composite.get-composite-product-by-query-shelter-row.notes}"
    )
    @ApiResponses(value = {
        @ApiResponse(code = 400, message = "BAD REQUEST, invalid request format."),
        @ApiResponse(code = 404, message = "NOT FOUND, shelter ID does NOT exist."),
        @ApiResponse(code = 422, message = "UNPROCESSABLE ENTITY, input parameters invalid.")
    })
    @GetMapping(
        value    = "/product-composite-by-query-shelter-row",
        produces = "application/json"
    )
    @ResponseBody
    Mono<ProductAggregate> getCompositeProductByQueryShelterRow(
        @RequestParam(defaultValue = "00-00")      String shelter,
        @RequestParam(defaultValue = "1000000000") String row
    );
    
    /**
     * TH: represents reactive programming (see below) !!
     */
    
    /**
     * TH: helps insert all product recommendations per (shelter,row), with reactive programming.
     * TH: curl http://localhost:8080/product-composite-by-insert-shelter-row/?shelter=22-01&row=0&index=2&side=0&productId=X
     * TH: https://www.baeldung.com/spring-request-param
     */
    @ApiOperation(
        value = "${api.product-composite.get-composite-product-by-insert-shelter-row.description}",
        notes = "${api.product-composite.get-composite-product-by-insert-shelter-row.notes}"
    )
    @ApiResponses(value = {
        @ApiResponse(code = 400, message = "BAD REQUEST, invalid request format."),
        @ApiResponse(code = 404, message = "NOT FOUND, shelter ID does NOT exist."),
        @ApiResponse(code = 422, message = "UNPROCESSABLE ENTITY, input parameters invalid.")
    })
    @GetMapping(
        value    = "/product-composite-by-insert-shelter-row",
        produces = "application/json"
    )
    @ResponseBody
    Integer getCompositeProductByInsertShelterRow(
        @RequestParam(defaultValue = "00-00")      String shelter,
        @RequestParam(defaultValue = "1000000000") String row,
        @RequestParam(defaultValue = "0")          String index,
        @RequestParam(defaultValue = "0")          String side,
        @RequestParam(defaultValue = "X")          String productId
    );
    
    /**
     * TH: helps remove all product recommendations per (shelter,row), with reactive programming.
     * TH: curl http://localhost:8080/product-composite-by-remove-shelter-row/?"shelter=22-02&row=0&setSortedString=1-2-4-5&setSortedBarString=3-6-7&setTotalString=1-2-3"
     * TH: https://www.baeldung.com/spring-request-param
     */
    @ApiOperation(
        value = "${api.product-composite.get-composite-product-by-remove-shelter-row.description}",
        notes = "${api.product-composite.get-composite-product-by-remove-shelter-row.notes}"
    )
    @ApiResponses(value = {
        @ApiResponse(code = 400, message = "BAD REQUEST, invalid request format."),
        @ApiResponse(code = 404, message = "NOT FOUND, shelter ID does NOT exist."),
        @ApiResponse(code = 422, message = "UNPROCESSABLE ENTITY, input parameters invalid.")
    })
    @GetMapping(
        value    = "/product-composite-by-remove-shelter-row",
        produces = "application/json"
    )
    @ResponseBody
    Integer getCompositeProductByRemoveShelterRow(
        @RequestParam(defaultValue = "00-00")                   String shelter,
        @RequestParam(defaultValue = "-1")                      String row,
        @RequestParam(defaultValue = "setSortedStringDummy")    String setSortedString,
        @RequestParam(defaultValue = "setSortedBarStringDummy") String setSortedBarString,
        @RequestParam(defaultValue = "setTotalStringDummy")     String setTotalString
    );
    
    /**
     * TH: helps submit all product recommendations per (shelter,row), with reactive programming.
     * TH: issues delete ONLY:
     * TH: curl http://localhost:8080/product-composite-by-submit-shelter-row/?"shelter=22-02&row=AND-0-AND-2-AND-3-&setSortedStringProductId=AND-AND-AND-&setSortedString=AND-AND-AND-&setSortedBarString=AND-2050705463-1126431793-2076358900-1082635082-AND-1500734307-1710855681-AND-1330678785-691502526-1833951241-155851167-&setTotalString=AND-2-3-4-5-AND-5-6-AND-2-3-4-5-&setProductIdRemove=AND-393766718-1276622499-AND-1307606527-AND-2043108443-909990426-"
     * TH: issues add and delete TOGETHER:
     * TH: curl ...
     * TH: https://www.baeldung.com/spring-request-param
     */
    @ApiOperation(
        value = "${api.product-composite.get-composite-product-by-submit-shelter-row.description}",
        notes = "${api.product-composite.get-composite-product-by-submit-shelter-row.notes}"
    )
    @ApiResponses(value = {
        @ApiResponse(code = 400, message = "BAD REQUEST, invalid request format."),
        @ApiResponse(code = 404, message = "NOT FOUND, shelter ID does NOT exist."),
        @ApiResponse(code = 422, message = "UNPROCESSABLE ENTITY, input parameters invalid.")
    })
    @GetMapping(
        value    = "/product-composite-by-submit-shelter-row",
        produces = "application/json"
    )
    @ResponseBody
    Integer getCompositeProductBySubmitShelterRow(
        @RequestParam(defaultValue = "00-00")                         String shelter,
        @RequestParam(defaultValue = "-1")                            String row,
        @RequestParam(defaultValue = SETSORTEDSTRINGPRODUCTIDDUMMY)   String setSortedStringProductId,
        @RequestParam(defaultValue = "setSortedStringDummy")          String setSortedString,
        @RequestParam(defaultValue = "setSortedBarStringDummy")       String setSortedBarString,
        @RequestParam(defaultValue = "setTotalStringDummy")           String setTotalString,
        @RequestParam(defaultValue = "setProductIdRemoveDummy")       String setProductIdRemove
    );
    
    /**
     * TH: represents reactive programming (see above) !!
     */
    
    /**
     * TH: helps delete product recommendations by recommendationId.
     * TH: curl http://localhost:8080/product-composite-by-delete-recommendations/?productId=X&recommendationId=0
     * TH: https://www.baeldung.com/spring-request-param
     */
    @ApiOperation(
        value = "${api.product-composite.product-composite-by-delete-recommendations.description}",
        notes = "${api.product-composite.product-composite-by-delete-recommendations.notes}"
    )
    @ApiResponses(value = {
        @ApiResponse(code = 400, message = "BAD REQUEST, invalid request format."),
        @ApiResponse(code = 404, message = "NOT FOUND, Recommendation ID does NOT exist."),
        @ApiResponse(code = 422, message = "UNPROCESSABLE ENTITY, input parameters invalid.")
    })
    @GetMapping(
        value    = "/product-composite-by-delete-recommendations",
        produces = "application/json"
    )
    @ResponseBody
    Integer deleteRecommendationsByRecommendationId(
        @RequestParam(defaultValue = "X") String productId,
        @RequestParam(defaultValue = "0") String recommendationId
    );
    
    /**
     * TH: helps insert product recommendations by given attributes.
     * TH: curl http://localhost:8080/product-composite-by-insert-recommendations/?productId=X&recommendationId=0&author=X&shelter=22-01&row=0&offset=0&url=https://cdn2.thecatapi.com/images/15r.jpg
     * TH: https://www.baeldung.com/spring-request-param
     */
    @ApiOperation(
        value = "${api.product-composite.product-composite-by-insert-recommendations.description}",
        notes = "${api.product-composite.product-composite-by-insert-recommendations.notes}"
    )
    @ApiResponses(value = {
        @ApiResponse(code = 400, message = "BAD REQUEST, invalid request format."),
//      @ApiResponse(code = 404, message = "NOT FOUND, Recommendation ID does NOT exist."),
        @ApiResponse(code = 422, message = "UNPROCESSABLE ENTITY, input parameters invalid.")
    })
    @GetMapping(
        value    = "/product-composite-by-insert-recommendations",
        produces = "application/json"
    )
    @ResponseBody
    Integer insertRecommendationsByAttributes(
        @RequestParam(defaultValue = "productId") String productId,
        @RequestParam(defaultValue = "recommendationId") String recommendationId,
        @RequestParam(defaultValue = "author") String author,
        @RequestParam(defaultValue = "shelter") String shelter,
        @RequestParam(defaultValue = "row") String row,
        @RequestParam(defaultValue = "offset") String offset,
        @RequestParam(defaultValue = "url") String url
    );
    
}

