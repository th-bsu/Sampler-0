package se.simple.microservices.composite.product;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import se.simple.api.composite.product.ProductAggregate;
import se.simple.api.composite.product.RecommendationSummary;
import se.simple.api.composite.product.ReviewSummary;
import se.simple.api.core.product.Product;
import se.simple.api.core.recommendation.Recommendation;
import se.simple.api.core.review.Review;
import se.simple.api.event.Event;
import se.simple.microservices.composite.product.services.ProductCompositeIntegration;

import java.util.concurrent.BlockingQueue;

import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.cloud.stream.test.matcher.MessageQueueMatcher.receivesPayloadThat;
import static org.springframework.http.HttpStatus.OK;
import static reactor.core.publisher.Mono.just;
import static se.simple.api.event.Event.Type.CREATE;
import static se.simple.api.event.Event.Type.DELETE;
import static se.simple.microservices.composite.product.IsSameEvent.sameEventExceptCreatedAt;

// TH: helps implement standard JUnit4-based unit and integration testing (SpringRunner), agnostic of testing framework.
// TH: https://zetcode.com/spring/springrunner/#:~:text=SpringRunner%20is%20an%20alias%20for,based%20unit%20and%20integration%20tests.
// TH: helps create web application context (i.e. webEnvironment, reactive or servlet) and listens on random port.
// TH: helps override default value of some property for testing purposes.
// TH: https://reflectoring.io/spring-boot-test/
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment=RANDOM_PORT, properties = {"eureka.client.enabled=false"})
public class MessagingTests {

	private static final int PRODUCT_ID_OK = 1;
	private static final int PRODUCT_ID_NOT_FOUND = 2;
	private static final int PRODUCT_ID_INVALID = 3;

    // TH: helps with Spring's dependency injection (Autowired).
    // TH: helps create client for testing web server (i.e. performs request and verifies response, over HTTP or WebFlux).
    @Autowired
    private WebTestClient client;

	@Autowired
	private ProductCompositeIntegration.MessageSources channels;

	// TH: helps maintain map between output channels and messages received (i.e. FIFO), and run assertions on enqueued messages.
	@Autowired
	private MessageCollector collector;

	// TH: helps support operations that can 'wait' for queue: until non-empty during load and until available during store.
	BlockingQueue<Message<?>> queueProducts = null;
	BlockingQueue<Message<?>> queueRecommendations = null;
	BlockingQueue<Message<?>> queueReviews = null;

	// TH: helps prepare environment before '@Test' methods invoked (i.e. resource allocation).
	@Before
	public void setUp() {
		queueProducts = getQueue(channels.outputProducts());
		queueRecommendations = getQueue(channels.outputRecommendations());
		queueReviews = getQueue(channels.outputReviews());
	}

	@Test
	public void createCompositeProduct1() {
		
		ProductAggregate composite = new ProductAggregate(1, "name", 1, 0.0, null, null, null, null);
		postAndVerifyProduct(composite, OK);

		// Assert one expected new product events queued up
		assertEquals(1, queueProducts.size());

		Event<Integer, Product> expectedEvent = new Event(
		   CREATE, 
		   composite.getProductId(), 
		   new Product(
		      composite.getProductId(), 
		      composite.getName(), 
		      composite.getAmount(), 
		      "ABCD/127.0.1.1:0",
		      composite.getValue(),
		      composite.getUrl()
		   )
		);
		// TH: helps assert Matcher implementation from incoming messages in output channel of MessageProcessor (i.e. Product).
		assertThat(queueProducts, is(receivesPayloadThat(sameEventExceptCreatedAt(expectedEvent))));

		// Assert none recommendations and review events
		assertEquals(0, queueRecommendations.size());
		assertEquals(0, queueReviews.size());
	}

	@Test
	public void createCompositeProduct2() {
		
		String url = "https://cdn2.thecatapi.com/images/25r.jpg";
		
		ProductAggregate composite = new ProductAggregate(
		        1, 
		        "name", 
		        1, 
		        0.0,
			singletonList(
			   new RecommendationSummary(1, "a", 1, 1, "19-08", 214, url)
			),
			singletonList(new ReviewSummary(1, "a", "s", "c")), 
			null,
			url
			);

		postAndVerifyProduct(composite, OK);

		// Assert one create product event queued up
		assertEquals(1, queueProducts.size());

		Event<Integer, Product> expectedProductEvent = new Event(
		   CREATE, 
		   composite.getProductId(), 
		   new Product(
		      composite.getProductId(), 
		      composite.getName(), 
		      composite.getAmount(), 
		      "ABCD/127.0.1.1:0",
		      composite.getValue(),
		      composite.getUrl()
		   )
		);
		// TH: helps assert Matcher implementation from incoming messages in output channel of MessageProcessor (i.e. Product).
		assertThat(queueProducts, receivesPayloadThat(sameEventExceptCreatedAt(expectedProductEvent)));

		// Assert one create recommendation event queued up
		assertEquals(1, queueRecommendations.size());

		RecommendationSummary rec = composite.getRecommendations().get(0);
		Event<Integer, Product> expectedRecommendationEvent = new Event(
		   CREATE, 
		   composite.getProductId(), 
		   new Recommendation(
		      composite.getProductId(), 
		      rec.getRecommendationId(), 
		      rec.getAuthor(), 
		      rec.getRow(), 
		      rec.getOffset(), 
		      null, 
		      "19-08",
		      url
		   )
		);
		// TH: helps assert Matcher implementation from incoming messages in output channel of MessageProcessor (i.e. Recommendation).
		assertThat(queueRecommendations, receivesPayloadThat(sameEventExceptCreatedAt(expectedRecommendationEvent)));

		// Assert one create review event queued up
		assertEquals(1, queueReviews.size());

		ReviewSummary rev = composite.getReviews().get(0);
		Event<Integer, Product> expectedReviewEvent = new Event(
		   CREATE, 
		   composite.getProductId(), 
		   new Review(
		      composite.getProductId(), 
		      rev.getReviewId(), 
		      rev.getAuthor(), 
		      rev.getStatus(), 
		      rev.getContent(), 
		      null
		   )
		);
		// TH: helps assert Matcher implementation from incoming messages in output channel of MessageProcessor (i.e. Review).
		assertThat(queueReviews, receivesPayloadThat(sameEventExceptCreatedAt(expectedReviewEvent)));
		
	}

	@Test
	public void deleteCompositeProduct() {

		deleteAndVerifyProduct(1, OK);

		// Assert one delete product event queued up
		assertEquals(1, queueProducts.size());

		// TH: helps assert Matcher implementation from incoming messages in output channel of MessageProcessor (i.e. Product).
		// TH: Event.getKey == productId.
		Event<Integer, Product> expectedEvent = new Event(DELETE, 1, null);
		assertThat(queueProducts, is(receivesPayloadThat(sameEventExceptCreatedAt(expectedEvent))));

		// Assert one delete recommendation event queued up
		assertEquals(1, queueRecommendations.size());

		// TH: helps assert Matcher implementation from incoming messages in output channel of MessageProcessor (i.e. Recommendation).
		// TH: Event.getKey == productId.
		Event<Integer, Product> expectedRecommendationEvent = new Event(DELETE, 1, null);
		assertThat(queueRecommendations, receivesPayloadThat(sameEventExceptCreatedAt(expectedRecommendationEvent)));

		// Assert one delete review event queued up
		assertEquals(1, queueReviews.size());

		// TH: helps assert Matcher implementation from incoming messages in output channel of MessageProcessor (i.e. Review).
		// TH: Event.getKey == productId.
		Event<Integer, Product> expectedReviewEvent = new Event(DELETE, 1, null);
		assertThat(queueReviews, receivesPayloadThat(sameEventExceptCreatedAt(expectedReviewEvent)));
	}

	// TH: helps obtain queue that will receive messages sent to given channel.
	private BlockingQueue<Message<?>> getQueue(MessageChannel messageChannel) {
		return collector.forChannel(messageChannel);
	}

	// TH: prepares HTTP post request, specifies URI, set body from producer (i.e. Mono), performs exchange without body, asserts on response status.
	private void postAndVerifyProduct(ProductAggregate compositeProduct, HttpStatus expectedStatus) {
		client.post()
			.uri("/product-composite")
			.body(just(compositeProduct), ProductAggregate.class)
			.exchange()
			.expectStatus().isEqualTo(expectedStatus);
	}

	// TH: prepares HTTP delete request, specifies URI, performs exchange without body, asserts on response status.
	private void deleteAndVerifyProduct(int productId, HttpStatus expectedStatus) {
		client.delete()
			.uri("/product-composite/" + productId)
			.exchange()
			.expectStatus().isEqualTo(expectedStatus);
	}
}
