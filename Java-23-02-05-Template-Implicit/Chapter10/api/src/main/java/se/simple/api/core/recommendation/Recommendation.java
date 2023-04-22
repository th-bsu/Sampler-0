package se.simple.api.core.recommendation;

public class Recommendation {
    private int productId;
    private int recommendationId;
    private String author;
    private int row;
    private int offset;
    private String serviceAddress;
    private String shelter;
    private String url;

    public Recommendation() {
        productId = 0;
        recommendationId = 0;
        author = null;
        row = -1;
        offset = -1;
        serviceAddress = null;
        shelter = null;
        url = null;
    }

    public Recommendation(
       int productId, 
       int recommendationId, 
       String author, 
       int row, 
       int offset, 
       String serviceAddress, 
       String shelter,
       String url
    ) {
        this.productId = productId;
        this.recommendationId = recommendationId;
        this.author = author;
        this.row = row;
        this.offset = offset;
        this.serviceAddress = serviceAddress;
        this.shelter = shelter;
        this.url = url;
    }

    public int getProductId() {
        return productId;
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

    public String getServiceAddress() {
        return serviceAddress;
    }

    public String getShelter() {
        return shelter;
    }
    
    public String getUrl(){
       return url;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public void setRecommendationId(int recommendationId) {
        this.recommendationId = recommendationId;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public void setServiceAddress(String serviceAddress) {
        this.serviceAddress = serviceAddress;
    }
    
    public void setShelter(String shelter) {
        this.shelter = shelter;
    }
    
    public void setUrl(String url) {
       this.url = url;
    }
    
}

