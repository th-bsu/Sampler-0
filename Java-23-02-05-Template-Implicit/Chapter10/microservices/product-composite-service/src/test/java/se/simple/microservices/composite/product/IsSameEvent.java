package se.simple.microservices.composite.product;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.simple.api.event.Event;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

// TH: extends convenient base class for Matchers that require non-null value of specific type.
public class IsSameEvent extends TypeSafeMatcher<String> {

    private static final Logger LOG = LoggerFactory.getLogger(IsSameEvent.class);

    // TH: ...
    private ObjectMapper mapper = new ObjectMapper();

    private Event expectedEvent;


    private IsSameEvent(Event expectedEvent) {
        this.expectedEvent = expectedEvent;
    }

    // TH: helps compare events without specific key (i.e. explicitly removed).
    @Override
    protected boolean matchesSafely(String eventAsJson) {

        if (expectedEvent == null) return false;

        LOG.trace("Convert the following json string to a map: {}", eventAsJson);
        Map mapEvent = convertJsonStringToMap(eventAsJson);
        mapEvent.remove("eventCreatedAt");

        Map mapExpectedEvent = getMapWithoutCreatedAt(expectedEvent);

        LOG.trace("Got the map: {}", mapEvent);
        LOG.trace("Compare to the expected map: {}", mapExpectedEvent);
        return mapEvent.equals(mapExpectedEvent);
    }

    @Override
    public void describeTo(Description description) {
        String expectedJson = convertObjectToJsonString(expectedEvent);
        description.appendText("expected to look like " + expectedJson);
    }

    public static Matcher<String> sameEventExceptCreatedAt(Event expectedEvent) {
        return new IsSameEvent(expectedEvent);
    }

   	private Map getMapWithoutCreatedAt(Event event) {
        Map mapEvent = convertObjectToMap(event);
        mapEvent.remove("eventCreatedAt");
        return mapEvent;
    }

    // TH: helps convert instance of class (i.e. Event) into JsonNode (i.e. base class for all JSON nodes), and then into instance of another class (i.e. Map).
    private Map convertObjectToMap(Object object) {
   		JsonNode node = mapper.convertValue(object, JsonNode.class);
   		return mapper.convertValue(node, Map.class);
   	}

    // TH: helps serialize Java value as String.
    private String convertObjectToJsonString(Object object) {
   		try {
   			return mapper.writeValueAsString(object);
   		} catch (JsonProcessingException e) {
   			throw new RuntimeException(e);
   		}
   	}

    // TH: helps deserialize JSON content from given JSON string.
    private Map convertJsonStringToMap(String eventAsJson) {
        try {
            return mapper.readValue(eventAsJson, new TypeReference<HashMap>(){});
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
