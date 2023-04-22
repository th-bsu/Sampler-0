package se.simple.microservices.core.product;

import org.junit.Test;
import org.mapstruct.factory.Mappers;
import se.simple.api.core.product.Product;
import se.simple.microservices.core.product.persistence.ProductEntity;
import se.simple.microservices.core.product.services.ProductMapper;

import static org.junit.Assert.*;

public class MapperTests {

    // TH: helps convert between API and Entity.
    private ProductMapper mapper = Mappers.getMapper(ProductMapper.class);

    @Test
    public void mapperTests() {

        assertNotNull(mapper);

        Product api = new Product(1, "n", 1, "sa", 0.0, "https://cdn2.thecatapi.com/images/25r.jpg");

        ProductEntity entity = mapper.apiToEntity(api);

        assertEquals(api.getProductId(), entity.getProductId());
        assertEquals(api.getProductId(), entity.getProductId());
        assertEquals(api.getName(), entity.getName());
        assertEquals(api.getAmount(), entity.getAmount());

        Product api2 = mapper.entityToApi(entity);

        assertEquals(api.getProductId(), api2.getProductId());
        assertEquals(api.getProductId(), api2.getProductId());
        assertEquals(api.getName(),      api2.getName());
        assertEquals(api.getAmount(),    api2.getAmount());
        assertNull(api2.getServiceAddress());
    }
}
