package se.simple.api.core.product;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

public interface ProductService {

    // TH: helps bound method parameter to body of web request.
    Product createProduct(@RequestBody Product body);

    /**
     * Sample usage: curl $HOST:$PORT/product/1
     *
     * @param productId
     * @return the product, if found, else null
     */
    @GetMapping(
        value    = "/product/{productId}",
        produces = "application/json")
     Mono<Product> getProduct(@PathVariable int productId);

    // TH: helps bound method parameter to URI template variable.
    void deleteProduct(@PathVariable int productId);
}
