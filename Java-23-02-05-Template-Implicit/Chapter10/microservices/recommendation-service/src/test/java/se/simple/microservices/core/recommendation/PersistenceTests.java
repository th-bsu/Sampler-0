package se.simple.microservices.core.recommendation;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.test.context.junit4.SpringRunner;
import se.simple.microservices.core.recommendation.persistence.RecommendationEntity;
import se.simple.microservices.core.recommendation.persistence.RecommendationRepository;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@DataMongoTest
public class PersistenceTests {

    @Autowired
    private RecommendationRepository repository;

    private RecommendationEntity savedEntity;

    @Before public void setupDb() {
        
   	repository.deleteAll().block();
   	
   	String shelter = "19-02";
        RecommendationEntity entity = new RecommendationEntity(1, 2, "a", 3, 0, shelter, "No-URL-At-All");
        savedEntity = repository.save(entity).block();

        assertEqualsRecommendation(entity, savedEntity);
    }


    @Test public void create() {

        String shelter = "19-02";
        RecommendationEntity newEntity = new RecommendationEntity(1, 3, "a", 3, 1, shelter, "Dummy-URL");
        repository.save(newEntity).block();

        RecommendationEntity foundEntity = repository.findById(newEntity.getId()).block();
        assertEqualsRecommendation(newEntity, foundEntity);

        assertEquals(2, (long)repository.count().block());
    }

    @Test
   	public void update() {
        savedEntity.setAuthor("a2");
        repository.save(savedEntity).block();

        RecommendationEntity foundEntity = repository.findById(savedEntity.getId()).block();
        assertEquals(1, (long)foundEntity.getVersion());
        assertEquals("a2", foundEntity.getAuthor());
    }

    @Test
   	public void delete() {
        repository.delete(savedEntity).block();
        assertFalse(repository.existsById(savedEntity.getId()).block());
    }

    @Test
   	public void getByProductId() {
        List<RecommendationEntity> entityList = repository.findByProductId(savedEntity.getProductId()).collectList().block();

        assertThat(entityList, hasSize(1));
        assertEqualsRecommendation(savedEntity, entityList.get(0));
    }

    @Test(expected = DuplicateKeyException.class)
    public void duplicateError() {
        String shelter = "19-02";
        RecommendationEntity entity     = new RecommendationEntity(1, 3, "b", 3, 1, shelter, "No-URL-Provided");
        repository.save(entity).block();
        RecommendationEntity entity_dup = new RecommendationEntity(1, 3, "c", 3, 1, shelter, "NA");
        repository.save(entity_dup).block();
    }

    @Test
   	public void optimisticLockError() {

        // Store the saved entity in two separate entity objects
        RecommendationEntity entity1 = repository.findById(savedEntity.getId()).block();
        RecommendationEntity entity2 = repository.findById(savedEntity.getId()).block();

        // Update the entity using the first entity object
        entity1.setAuthor("a1");
        repository.save(entity1).block();

        //  Update the entity using the second entity object.
        // This should fail since the second entity now holds a old version number, i.e. a Optimistic Lock Error
        try {
            entity2.setAuthor("a2");
            repository.save(entity2).block();

            fail("Expected an OptimisticLockingFailureException");
        } catch (OptimisticLockingFailureException e) {}

        // Get the updated entity from the database and verify its new sate
        RecommendationEntity updatedEntity = repository.findById(savedEntity.getId()).block();
        assertEquals(1, (int)updatedEntity.getVersion());
        assertEquals("a1", updatedEntity.getAuthor());
    }

    private void assertEqualsRecommendation(RecommendationEntity expectedEntity, RecommendationEntity actualEntity) {
        assertEquals(expectedEntity.getId(),               actualEntity.getId());
        assertEquals(expectedEntity.getVersion(),          actualEntity.getVersion());
        assertEquals(expectedEntity.getProductId(),        actualEntity.getProductId());
        assertEquals(expectedEntity.getRecommendationId(), actualEntity.getRecommendationId());
        assertEquals(expectedEntity.getAuthor(),           actualEntity.getAuthor());
        assertEquals(expectedEntity.getRow(),              actualEntity.getRow());
        assertEquals(expectedEntity.getOffset(),           actualEntity.getOffset());
    }
    
}

