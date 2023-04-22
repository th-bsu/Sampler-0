package se.simple.api.composite.product;

import java.util.List;

/**
 * TH: helps return composite product to front-end request.
 */
public class ProductAggregate {
    private final int productId;
    private final String name;
    private final int amount;
    private final double value;
    private final List<RecommendationSummary> recommendations;
    private final List<ReviewSummary> reviews;
    private final ServiceAddresses serviceAddresses;
    private final String url;

    public ProductAggregate() {
        productId = 0;
        name = null;
        amount = 0;
        value = 0.0;
        recommendations = null;
        reviews = null;
        serviceAddresses = null;
        url = null;
    }

    public ProductAggregate(
        int productId,
        String name,
        int amount,
        double value,
        List<RecommendationSummary> recommendations,
        List<ReviewSummary> reviews,
        ServiceAddresses serviceAddresses,
        String url
    ) {
        this.productId = productId;
        this.name = name;
        this.amount = amount;
        this.value = value;
        this.recommendations = recommendations;
        this.reviews = reviews;
        this.serviceAddresses = serviceAddresses;
        this.url = url;
    }

    public int getProductId() {
        return productId;
    }

    public String getName() {
        return name;
    }

    public int getAmount() {
        return amount;
    }

    public double getValue() {
        return value;
    }

    public List<RecommendationSummary> getRecommendations() {
        return recommendations;
    }

    public List<ReviewSummary> getReviews() {
        return reviews;
    }

    public ServiceAddresses getServiceAddresses() {
        return serviceAddresses;
    }
    
    public String getUrl(){
       return url;
    }
    
}
