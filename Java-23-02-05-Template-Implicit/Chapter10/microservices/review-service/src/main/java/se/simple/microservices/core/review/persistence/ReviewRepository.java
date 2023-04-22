package se.simple.microservices.core.review.persistence;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ReviewRepository extends CrudRepository<ReviewEntity, Integer> {

    // TH: helps indicate transactions are READ-ONLY.
    // TH: will NOT cause failure on write access attempts.
    @Transactional(readOnly = true)
    List<ReviewEntity> findByProductId(int productId);
}
