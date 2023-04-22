package se.simple.microservices.core.product;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.http.HttpStatus;
import org.springframework.integration.channel.AbstractMessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import se.simple.api.core.product.Product;
import se.simple.api.event.Event;
import se.simple.microservices.core.product.persistence.ProductRepository;
import se.simple.util.exceptions.InvalidInputException;

import static org.junit.Assert.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static se.simple.api.event.Event.Type.CREATE;
import static se.simple.api.event.Event.Type.DELETE;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment=RANDOM_PORT, properties = {"spring.data.mongodb.port: 0", "eureka.client.enabled=false"})
public class ProductServiceApplicationTests {

    @Autowired
    private WebTestClient client;

	@Autowired
	private ProductRepository repository;

	// TH: references MessageProcessor (i.e. Product, EnableBinding).
	@Autowired
	private Sink channels;

	// TH: provides common properties to MessageChannel implementations.
	// TH: provides common functionality to send and receive messages.
	private AbstractMessageChannel input = null;

	@Before
	public void setupDb() {
		// TH: retrieves MessageChannel, which maintains registry of subscribers and invokes them to handle messages sent thru this channel.
		// TH: helps define methods to send messages.
		input = (AbstractMessageChannel) channels.input();
		repository.deleteAll().block();
	}

	// TH: deploys each unit-test separately.
	@Test
	public void getProductById() {

		int productId = 1;

		assertNull(repository.findByProductId(productId).block());
		assertEquals(0, (long)repository.count().block());

		sendCreateProductEvent(productId);

		assertNotNull(repository.findByProductId(productId).block());
		assertEquals(1, (long)repository.count().block());

		getAndVerifyProduct(productId, OK)
            .jsonPath("$.productId").isEqualTo(productId);
	}

	// TH: deploys each unit-test separately.
	@Test
	public void duplicateError() {

		int productId = 1;

		assertNull(repository.findByProductId(productId).block());

		sendCreateProductEvent(productId);

		assertNotNull(repository.findByProductId(productId).block());

		try {
			sendCreateProductEvent(productId);
			fail("Expected a MessagingException here!");
		} catch (MessagingException me) {
			if (me.getCause() instanceof InvalidInputException)	{
				InvalidInputException iie = (InvalidInputException)me.getCause();
				assertEquals("Duplicate key, Product Id: " + productId, iie.getMessage());
			} else {
				fail("Expected a InvalidInputException as the root cause!");
			}
		}
	}

	@Test
	public void deleteProduct() {

		int productId = 1;

		sendCreateProductEvent(productId);
		assertNotNull(repository.findByProductId(productId).block());

		sendDeleteProductEvent(productId);
		assertNull(repository.findByProductId(productId).block());

		sendDeleteProductEvent(productId);
	}

	@Test
	public void getProductInvalidParameterString() {

		getAndVerifyProduct("/no-integer", BAD_REQUEST)
            .jsonPath("$.path").isEqualTo("/product/no-integer")
            .jsonPath("$.message").isEqualTo("Type mismatch.");
	}

	@Test
	public void getProductNotFound() {

		int productIdNotFound = 13;
		getAndVerifyProduct(productIdNotFound, NOT_FOUND)
            .jsonPath("$.path").isEqualTo("/product/" + productIdNotFound)
            .jsonPath("$.message").isEqualTo("No product found for productId: " + productIdNotFound);
	}

	@Test
	public void getProductInvalidParameterNegativeValue() {

        int productIdInvalid = -1;

		getAndVerifyProduct(productIdInvalid, UNPROCESSABLE_ENTITY)
            .jsonPath("$.path").isEqualTo("/product/" + productIdInvalid)
            .jsonPath("$.message").isEqualTo("Invalid productId: " + productIdInvalid);
	}

	private WebTestClient.BodyContentSpec getAndVerifyProduct(int productId, HttpStatus expectedStatus) {
		return getAndVerifyProduct("/" + productId, expectedStatus);
	}

	private WebTestClient.BodyContentSpec getAndVerifyProduct(String productIdPath, HttpStatus expectedStatus) {
		return client.get()
			.uri("/product" + productIdPath)
			.accept(APPLICATION_JSON)
			.exchange()
			.expectStatus().isEqualTo(expectedStatus)
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody();
	}

	// TH: sends message on given channel, based on Event for given Product.
	private void sendCreateProductEvent(int productId) {
		Product product = new Product(
		   productId, 
		   "Name " + productId, 
		   productId, 
		   "SA", 
		   0.0,
		   "Dummy-URL"
		);
		Event<Integer, Product> event = new Event(CREATE, productId, product);
		input.send(new GenericMessage<>(event));
	}

	private void sendDeleteProductEvent(int productId) {
		Event<Integer, Product> event = new Event(DELETE, productId, null);
		input.send(new GenericMessage<>(event));
	}
}
