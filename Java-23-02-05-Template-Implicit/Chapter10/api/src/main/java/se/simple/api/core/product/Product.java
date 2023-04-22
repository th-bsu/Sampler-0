package se.simple.api.core.product;

public class Product {

    // TH: represents product identification (i.e. matches Entity).
    private int productId;
    
    // TH: represents product name.
    private String name;
    
    // TH: represents total amount (i.e. minimum).
    private int amount;
    
    // TH: represents most-recent value (i.e. after review complete).
    private double value;
    
    // TH: represents microservice address.
    private String serviceAddress;
    
    // TH: represents URL.
    private String url;

    public Product() {
        productId = 0;
        name = null;
        amount = 0;
        value = 0;
        serviceAddress = null;
        url = null;
    }

    public Product(int productId, String name, int amount, String serviceAddress, double value, String url) {
        this.productId = productId;
        this.name = name;
        this.amount = amount;
        this.value = value;
        this.serviceAddress = serviceAddress;
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

    public String getServiceAddress() {
        return serviceAddress;
    }
    
    public String getUrl(){
       return url;
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

    public void setServiceAddress(String serviceAddress) {
        this.serviceAddress = serviceAddress;
    }
    
    public void setUrl(String url) {
       this.url = url;
    }
    
}
