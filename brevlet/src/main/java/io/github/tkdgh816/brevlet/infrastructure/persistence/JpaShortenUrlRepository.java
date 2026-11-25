package io.github.tkdgh816.brevlet.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

interface JpaShortenUrlRepository extends JpaRepository<ShortenUrlEntity, String>, JpaSpecificationExecutor<ShortenUrlEntity> {

}
