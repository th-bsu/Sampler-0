package se.simple.springcloud.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// TH: helps RestTemplate bean to use LoadBalancerClient.
import org.springframework.cloud.client.loadbalancer.LoadBalanced;

import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;

@SpringBootApplication
public class GatewayApplication {

	// TH: Bean         --- creates Bean for auto-injection.
	// TH: LoadBalanced --- connects with microservices registered with discovery server.
	@Bean
	@LoadBalanced
	public WebClient.Builder loadBalancedWebClientBuilder() {
		final WebClient.Builder builder = WebClient.builder();
		return builder;
	}

	public static void main(String[] args) {
		SpringApplication.run(GatewayApplication.class, args);
	}

}
