package se.simple.microservices.core.product.persistence;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import static java.lang.String.format;

// TH: helps persist domain object to MongoDB.
@Document(collection="products")
public class ProductEntity {

    @Id
    private String id;

    @Version
    private Integer version;

    @Indexed(unique = true)
    private int productId;
    private String name;
    private int amount;
    
    private double value;
    
    private String url;

    public ProductEntity() {
    }

    public ProductEntity(int productId, String name, int amount, double value, String url) {
        this.productId = productId;
        this.name = name;
        this.amount = amount;
        this.value = value;
        this.url = url;
    }

    @Override
    public String toString() {
        return format("ProductEntity: %s", productId);
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

    public String getName() {
        return name;
    }

    public int getAmount() {
        return amount;
    }

    public double getValue() {
        return value;
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

    public void setName(String name) {
        this.name = name;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public void setValue(double value) {
        this.value=value;
    }
    
    public void setUrl(String url) {
       this.url = url;
    }
    
}

