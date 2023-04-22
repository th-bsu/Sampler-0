package se.simple.microservices.core.product.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;
import se.simple.api.core.product.Product;
import se.simple.api.core.product.ProductService;
import se.simple.api.event.Event;
import se.simple.util.exceptions.EventProcessingException;

// TH: represents consumer application, as specified by MessageChannel in producer application. (i.e. @EnableBinding and application.yml).
// TH: https://docs.spring.io/spring-cloud-stream/docs/Brooklyn.RELEASE/reference/html/_getting_started.html
@EnableBinding(Sink.class)
public class MessageProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(MessageProcessor.class);

    private final ProductService productService;

    // TH: gets annotated for automatic dependency injection.
    @Autowired
    public MessageProcessor(ProductService productService) {
        this.productService = productService;
    }

    // TH: marks method to be listener to inputs declared via @EnableBinding (i.e. channels).
    @StreamListener(target = Sink.INPUT)
    public void process(Event<Integer, Product> event) {

        LOG.info("Process message created at {}...", event.getEventCreatedAt());

        switch (event.getEventType()) {

        case CREATE:
            Product product = event.getData();
            LOG.info("Create product with ID: {}", product.getProductId());
            productService.createProduct(product);
            break;

        case DELETE:
            int productId = event.getKey();
            LOG.info("Delete recommendations with ProductID: {}", productId);
            productService.deleteProduct(productId);
            break;

        default:
            String errorMessage = "Incorrect event type: " + event.getEventType() + ", expected a CREATE or DELETE event";
            LOG.warn(errorMessage);
            throw new EventProcessingException(errorMessage);
        }

        LOG.info("Message processing done!");
    }
}
