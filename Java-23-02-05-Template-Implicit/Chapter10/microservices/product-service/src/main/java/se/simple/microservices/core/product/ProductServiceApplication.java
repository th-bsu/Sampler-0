package se.simple.microservices.core.product;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.data.mongodb.core.index.IndexResolver;
import org.springframework.data.mongodb.core.index.MongoPersistentEntityIndexResolver;
import org.springframework.data.mongodb.core.index.ReactiveIndexOperations;
import org.springframework.data.mongodb.core.mapping.MongoPersistentEntity;
import org.springframework.data.mongodb.core.mapping.MongoPersistentProperty;
import se.simple.microservices.core.product.persistence.ProductEntity;

// TH: helps define application class with auto-configuration, component scan, and extra configuration.
@SpringBootApplication
@ComponentScan("se.simple")
public class ProductServiceApplication {

	private static final Logger LOG = LoggerFactory.getLogger(ProductServiceApplication.class);

	public static void main(String[] args) {

		// TH: helps run Spring application, with default settings.
		ConfigurableApplicationContext ctx = SpringApplication.run(ProductServiceApplication.class, args);

		String mongodDbHost = ctx.getEnvironment().getProperty("spring.data.mongodb.host");
		String mongodDbPort = ctx.getEnvironment().getProperty("spring.data.mongodb.port");
		LOG.info("Connected to MongoDb: " + mongodDbHost + ":" + mongodDbPort);
	}

	// TH: helps specify MongoDB executions in reactive way.
	@Autowired
	ReactiveMongoOperations mongoTemplate;

	// TH: helps mark method as listener for application events (i.e. when ApplicationContext either initialized or refreshed).
	@EventListener(ContextRefreshedEvent.class)
	public void initIndicesAfterStartup() {

		// TH: retrieves mapping between MongoDB persistent entity and MongoDB persistent property.
		MappingContext<? extends MongoPersistentEntity<?>, MongoPersistentProperty> mappingContext = mongoTemplate.getConverter().getMappingContext();
		
		// TH: helps inspect MongoPersistentEntity for MongoPersistentEntity to be indexed.
		IndexResolver resolver = new MongoPersistentEntityIndexResolver(mappingContext);

		// TH: returns reactive operations that can be performed on indexes.
		ReactiveIndexOperations indexOps = mongoTemplate.indexOps(ProductEntity.class);
		
		// TH: helps create IndexDefinitions for properties of given entity class.
		// TH: ensures index exists for collection indicated by entity class.
		// TH: subscribes to given Mono and blocks indefinitely until next signal is received.
		resolver.resolveIndexFor(ProductEntity.class).forEach(e -> indexOps.ensureIndex(e).block());
		
	}
}

