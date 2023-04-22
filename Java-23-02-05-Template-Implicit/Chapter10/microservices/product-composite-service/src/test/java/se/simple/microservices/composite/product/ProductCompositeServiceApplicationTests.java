package se.simple.microservices.composite.product;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import se.simple.api.core.product.Product;
import se.simple.api.core.recommendation.Recommendation;
import se.simple.api.core.review.Review;
import se.simple.microservices.composite.product.services.ProductCompositeIntegration;
import se.simple.util.exceptions.InvalidInputException;
import se.simple.util.exceptions.NotFoundException;

import static java.util.Collections.singletonList;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;

// TH: ...
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment=RANDOM_PORT, properties = {"eureka.client.enabled=false"})
public class ProductCompositeServiceApplicationTests {

	private static final int PRODUCT_ID_OK = 1;
	private static final int PRODUCT_ID_NOT_FOUND = 2;
	private static final int PRODUCT_ID_INVALID = 3;

    @Autowired
    private WebTestClient client;

	// TH: helps add mocks to Spring ApplicationContext.
	@MockBean
	private ProductCompositeIntegration compositeIntegration;

	@Before
	public void setUp() {

		// TH: helps add stubbing.
		// TH: https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html
		when(compositeIntegration.getProduct(PRODUCT_ID_OK)).
			thenReturn(
			   Mono.just(
			      new Product(
			         PRODUCT_ID_OK, 
			         "name", 
			         1, 
			         "mock-address", 
			         0.0,
			         "Dummy-URL"
			      )
			   )
			);

		when(compositeIntegration.getRecommendations(PRODUCT_ID_OK)).
			thenReturn(
			   Flux.fromIterable(
			      singletonList(
			         new Recommendation(
			            PRODUCT_ID_OK, 
			            1, 
			            "author", 
			            1, 
			            1, 
			            "mock address", 
			            "19-12",
			            "Dummy-URL-2"
			         )
			      )
			   )
			);

		when(compositeIntegration.getReviews(PRODUCT_ID_OK)).
			thenReturn(Flux.fromIterable(singletonList(new Review(PRODUCT_ID_OK, 1, "author", "subject", "content", "mock address"))));

		when(compositeIntegration.getProduct(PRODUCT_ID_NOT_FOUND)).thenThrow(new NotFoundException("NOT FOUND: " + PRODUCT_ID_NOT_FOUND));

		when(compositeIntegration.getProduct(PRODUCT_ID_INVALID)).thenThrow(new InvalidInputException("INVALID: " + PRODUCT_ID_INVALID));
	}

	@Test
	public void contextLoads() {
	}

	@Test
	public void getProductById() {

		// TH: helps inspect specific subset of body.
		// TH: https://github.com/json-path/JsonPath
		getAndVerifyProduct(PRODUCT_ID_OK, OK)
            .jsonPath("$.productId").isEqualTo(PRODUCT_ID_OK)
            .jsonPath("$.recommendations.length()").isEqualTo(1)
            .jsonPath("$.reviews.length()").isEqualTo(1);
	}

	@Test
	public void getProductNotFound() {

		getAndVerifyProduct(PRODUCT_ID_NOT_FOUND, NOT_FOUND)
            .jsonPath("$.path").isEqualTo("/product-composite/" + PRODUCT_ID_NOT_FOUND)
            .jsonPath("$.message").isEqualTo("NOT FOUND: " + PRODUCT_ID_NOT_FOUND);
	}

	@Test
	public void getProductInvalidInput() {

		getAndVerifyProduct(PRODUCT_ID_INVALID, UNPROCESSABLE_ENTITY)
            .jsonPath("$.path").isEqualTo("/product-composite/" + PRODUCT_ID_INVALID)
            .jsonPath("$.message").isEqualTo("INVALID: " + PRODUCT_ID_INVALID);
	}

	// TH: prepares HTTP get request, specifies URI, specifies acceptable media type, performs exchange without body, and declares expectations ...
	// TH: references @GetMapping(produces = "application/json") in interface.
	// TH: references @ApiResponses(value = { @ApiResponse(code = ...) }) in interface.
	private WebTestClient.BodyContentSpec getAndVerifyProduct(int productId, HttpStatus expectedStatus) {
		return client.get()
			.uri("/product-composite/" + productId)
			.accept(APPLICATION_JSON)
			.exchange()
			.expectStatus().isEqualTo(expectedStatus)
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody();
	}
}
