package se.simple.microservices.core.recommendation;

import org.junit.Test;
import org.mapstruct.factory.Mappers;
import se.simple.api.core.recommendation.Recommendation;
import se.simple.microservices.core.recommendation.persistence.RecommendationEntity;
import se.simple.microservices.core.recommendation.services.RecommendationMapper;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class MapperTests {

    private RecommendationMapper mapper = Mappers.getMapper(RecommendationMapper.class);

    @Test
    public void mapperTests() {

        assertNotNull(mapper);

        String shelter = "19-02";
        Recommendation api = new Recommendation(1, 2, "a", 4, 0, "addr", shelter, "Invalid-URL");

        RecommendationEntity entity = mapper.apiToEntity(api);

        assertEquals(api.getProductId(), entity.getProductId());
        assertEquals(api.getRecommendationId(), entity.getRecommendationId());
        assertEquals(api.getAuthor(), entity.getAuthor());
        assertEquals(api.getRow(), entity.getRow());
        assertEquals(api.getOffset(), entity.getOffset());

        Recommendation api2 = mapper.entityToApi(entity);

        assertEquals(api.getProductId(), api2.getProductId());
        assertEquals(api.getRecommendationId(), api2.getRecommendationId());
        assertEquals(api.getAuthor(), api2.getAuthor());
        assertEquals(api.getRow(), api2.getRow());
        assertEquals(api.getOffset(), api2.getOffset());
        assertNull(api2.getServiceAddress());
    }

    @Test
    public void mapperListTests() {

        assertNotNull(mapper);

        String shelter = "19-02";
        Recommendation api = new Recommendation(1, 2, "a", 4, 1, "addr", shelter, "Invalid-URL-Again");
        List<Recommendation> apiList = Collections.singletonList(api);

        List<RecommendationEntity> entityList = mapper.apiListToEntityList(apiList);
        assertEquals(apiList.size(), entityList.size());

        RecommendationEntity entity = entityList.get(0);

        assertEquals(api.getProductId(), entity.getProductId());
        assertEquals(api.getRecommendationId(), entity.getRecommendationId());
        assertEquals(api.getAuthor(), entity.getAuthor());
        assertEquals(api.getRow(), entity.getRow());
        assertEquals(api.getOffset(), entity.getOffset());

        List<Recommendation> api2List = mapper.entityListToApiList(entityList);
        assertEquals(apiList.size(), api2List.size());

        Recommendation api2 = api2List.get(0);

        assertEquals(api.getProductId(), api2.getProductId());
        assertEquals(api.getRecommendationId(), api2.getRecommendationId());
        assertEquals(api.getAuthor(), api2.getAuthor());
        assertEquals(api.getRow(), api2.getRow());
        assertEquals(api.getOffset(), api2.getOffset());
        assertNull(api2.getServiceAddress());
    }
}
