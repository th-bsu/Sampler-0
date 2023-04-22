package se.simple.microservices.composite.product;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import se.simple.api.core.product.Product;
import se.simple.api.event.Event;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static se.simple.api.event.Event.Type.CREATE;
import static se.simple.api.event.Event.Type.DELETE;
import static se.simple.microservices.composite.product.IsSameEvent.sameEventExceptCreatedAt;

public class IsSameEventTests {

	// TH: helps provide functionality to read and write JSON (i.e. POJOs or JsonNode).
	// TH: https://fasterxml.github.io/jackson-databind/javadoc/2.7/com/fasterxml/jackson/databind/ObjectMapper.html
	ObjectMapper mapper = new ObjectMapper();

    // TH: helps deploy JUnit test case (i.e. fresh instance and method invocation).
    // TH: https://junit.org/junit4/javadoc/4.12/org/junit/Test.html
    @Test
    public void testEventObjectCompare() throws JsonProcessingException {
    
    		// Event #1 and #2 are the same event, but occurs as different times
		// Event #3 and #4 are different events
		Event<Integer, Product> event1 = new Event<>(CREATE, 1, new Product(1, "name", 1, null, 0.0, "https://cdn2.thecatapi.com/images/15r.jpg"));
		Event<Integer, Product> event2 = new Event<>(CREATE, 1, new Product(1, "name", 1, null, 0.0, "https://cdn2.thecatapi.com/images/15r.jpg"));
		Event<Integer, Product> event3 = new Event<>(DELETE, 1, null);
		Event<Integer, Product> event4 = new Event<>(CREATE, 1, new Product(2, "name", 1, null, 0.0, "https://cdn2.thecatapi.com/images/17r.jpg"));

		// TH: helps serialize Java object as string.
		String event1JSon = mapper.writeValueAsString(event1);

		// TH: helps compare Java objects by data type.
		assertThat(event1JSon, is(sameEventExceptCreatedAt(event2)));
		assertThat(event1JSon, not(sameEventExceptCreatedAt(event3)));
		assertThat(event1JSon, not(sameEventExceptCreatedAt(event4)));
    }
}
