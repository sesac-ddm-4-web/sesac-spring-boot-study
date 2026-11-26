package com.example.personal_project.domain.item.repository;

import com.example.personal_project.domain.item.model.CollectionItem;
import com.example.personal_project.domain.item.model.SourceType;

import java.util.List;

// TODO: sourceType + externalId로 중복 여부를 조회하는 계약을 추가한다.
public interface ItemRepository {

    CollectionItem add(CollectionItem collectionItem);
    List<CollectionItem> findAll();
    CollectionItem findById(Long id);

    boolean existsBySourceTypeAndExternalId(SourceType sourceType, Long externalId);
}
