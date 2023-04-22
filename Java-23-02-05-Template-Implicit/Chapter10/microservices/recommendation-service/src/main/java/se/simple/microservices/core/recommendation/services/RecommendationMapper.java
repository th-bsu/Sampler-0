package se.simple.microservices.core.recommendation.services;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import se.simple.api.core.recommendation.Recommendation;
import se.simple.microservices.core.recommendation.persistence.RecommendationEntity;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RecommendationMapper {

    @Mappings({
        @Mapping(target = "row", source="entity.row"),
        @Mapping(target = "url", source="entity.url"),
        @Mapping(target = "serviceAddress", ignore = true)
    })
    Recommendation entityToApi(RecommendationEntity entity);

    @Mappings({
        @Mapping(target = "row", source="api.row"),
        @Mapping(target = "url", source="api.url"),
        @Mapping(target = "id", ignore = true),
        @Mapping(target = "version", ignore = true)
    })
    RecommendationEntity apiToEntity(Recommendation api);

    List<Recommendation> entityListToApiList(List<RecommendationEntity> entity);
    List<RecommendationEntity> apiListToEntityList(List<Recommendation> api);
}
