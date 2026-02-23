package com.mannaregen.repository;

import com.mannaregen.entity.ActionLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActionLogRepository extends JpaRepository<ActionLogEntity, Long> {

    List<ActionLogEntity> findByCharacterNameOrderByPerformedAtDesc(String characterName);
}
