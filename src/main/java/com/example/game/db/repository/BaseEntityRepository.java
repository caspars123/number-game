package com.example.game.db.repository;

import com.example.game.db.model.BaseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.QueryByExampleExecutor;

import java.util.UUID;

@NoRepositoryBean
public interface BaseEntityRepository<E extends BaseEntity> extends JpaRepository<E, UUID>, JpaSpecificationExecutor<E>, QueryByExampleExecutor<E> {
}
