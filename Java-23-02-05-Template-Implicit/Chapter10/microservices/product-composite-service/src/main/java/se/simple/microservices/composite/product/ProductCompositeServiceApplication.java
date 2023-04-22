package se.simple.microservices.composite.product;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.reactive.function.client.WebClient;
import se.simple.microservices.composite.product.services.ProductCompositeIntegration;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spring.web.plugins.Docket;

import static java.util.Collections.emptyList;
import static org.springframework.web.bind.annotation.RequestMethod.*;
import static springfox.documentation.builders.RequestHandlerSelectors.basePackage;
import static springfox.documentation.spi.DocumentationType.SWAGGER_2;

// TH: helps enable @EnableAutoConfiguration, @ComponentScan, and @Configuration.
// TH: helps inject beans using @Autowired.
// TH: https://docs.spring.io/spring-boot/docs/2.0.x/reference/html/using-boot-using-springbootapplication-annotation.html
@SpringBootApplication
@ComponentScan("se.simple")
public class ProductCompositeServiceApplication {

    // TH: helps inject default configurations into given beans.
    // TH: https://docs.spring.io/spring-boot/docs/1.5.6.RELEASE/reference/html/boot-features-external-config.html
    @Value("${api.common.version}")           String apiVersion;
    @Value("${api.common.title}")             String apiTitle;
    @Value("${api.common.description}")       String apiDescription;
    @Value("${api.common.termsOfServiceUrl}") String apiTermsOfServiceUrl;
    @Value("${api.common.license}")           String apiLicense;
    @Value("${api.common.licenseUrl}")        String apiLicenseUrl;
    @Value("${api.common.contact.name}")      String apiContactName;
    @Value("${api.common.contact.url}")       String apiContactUrl;
    @Value("${api.common.contact.email}")     String apiContactEmail;

	/**
	 * Will exposed on $HOST:$PORT/swagger-ui.html
	 * TH: represents object to be managed by Spring container.
	 * @return
	 */
	@Bean
	public Docket apiDocumentation() {
		
		// TH: helps expose back-end API to front-end component or 3rd-party integrator.
		// TH: https://www.baeldung.com/swagger-2-documentation-for-spring-rest-api
		// TH: http://springfox.github.io/springfox/javadoc/2.7.0/index.html?springfox/documentation/spring/web/plugins/Docket.html
		return new Docket(SWAGGER_2)
			.select()
			.apis(basePackage("se.simple.microservices.composite.product"))
			.paths(PathSelectors.any())
			.build()
				.globalResponseMessage(POST, emptyList())
				.globalResponseMessage(GET, emptyList())
				.globalResponseMessage(DELETE, emptyList())
				.apiInfo(new ApiInfo(
                    apiTitle,
                    apiDescription,
                    apiVersion,
                    apiTermsOfServiceUrl,
                    new Contact(apiContactName, apiContactUrl, apiContactEmail),
                    apiLicense,
                    apiLicenseUrl,
                    emptyList()
                ));
    }

	// TH: helps 'autowire' relationships between collaborating beans.
	// TH: https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/beans/factory/annotation/Autowired.html
	@Autowired
	ProductCompositeIntegration integration;

	// TH: helps create Load Balancer bean.
	@Bean
	@LoadBalanced
	public WebClient.Builder loadBalancedWebClientBuilder() {
		final WebClient.Builder builder = WebClient.builder();
		return builder;
	}

	// TH: boot-straps and launches Spring application from Java main method.
	public static void main(String[] args) {
		SpringApplication.run(ProductCompositeServiceApplication.class, args);
	}
}
