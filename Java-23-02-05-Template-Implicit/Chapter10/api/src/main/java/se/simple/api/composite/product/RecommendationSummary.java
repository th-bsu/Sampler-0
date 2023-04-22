package se.simple.api.composite.product;

import java.lang.Integer;

public class RecommendationSummary {

    private final int recommendationId;
    private final String author;
    private final int row;
    private final int offset;
    private final String shelter;
    private final int productId;
    private final String url;

    public RecommendationSummary() {
        this.recommendationId = 0;
        this.author = null;
        this.row = -1;
        this.offset = -1;
        this.shelter = null;
        this.productId=1;
        this.url = null;
    }

    public RecommendationSummary(int recommendationId, String author, int row, int offset, String shelter, int productId, String url) {
        this.recommendationId = recommendationId;
        this.author = author;
        this.row = row;
        this.offset = offset;
        this.shelter = shelter;
        this.productId = productId;
        this.url = url;
    }

    public int getRecommendationId() {
        return recommendationId;
    }

    public String getAuthor() {
        return author;
    }

    public int getRow() {
        return row;
    }

    public int getOffset() {
        return offset;
    }
    
    public String getShelter() {
        return shelter;
    }
    
    public int getProductId() {
        return productId;
    }
    
    public String getUrl(){
       return url;
    }
    
}

