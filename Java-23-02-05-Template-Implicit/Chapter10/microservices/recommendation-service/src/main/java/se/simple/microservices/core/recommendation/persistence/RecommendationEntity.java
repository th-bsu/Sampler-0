package se.simple.microservices.core.recommendation.persistence;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import static java.lang.String.format;

@Document(collection="recommendations")
@CompoundIndex(name = "prod-rec-id", unique = true, def = "{'productId': 1, 'recommendationId' : 1, 'row' : 1, 'offset' : 1, 'shelter' : 1}")
public class RecommendationEntity {

    @Id
    private String id;

    @Version
    private Integer version;

    private int productId;
    private int recommendationId;
    private String author;
    private int row;
    private int offset;
    private String shelter;
    private String url;

    public RecommendationEntity() {
    }

    public RecommendationEntity(int productId, int recommendationId, String author, int row, int offset, String shelter, String url) {
        this.productId = productId;
        this.recommendationId = recommendationId;
        this.author = author;
        this.row = row;
        this.offset = offset;
        this.shelter = shelter;
        this.url = url;
    }

    @Override
    public String toString() {
        return format("RecommendationEntity: %s/%d", productId, recommendationId);
    }

    public String getId() {
        return id;
    }

    public Integer getVersion() {
        return version;
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

    public String getShelter() {
        return shelter;
    }
    
    public String getUrl() {
       return url;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setVersion(Integer version) {
        this.version = version;
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

    public void setShelter(String shelter) {
        this.shelter = shelter;
    }
    
    public void setUrl(String url) {
       this.url = url;
    }

}
